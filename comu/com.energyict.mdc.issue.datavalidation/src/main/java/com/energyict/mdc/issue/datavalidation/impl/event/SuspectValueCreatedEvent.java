package com.energyict.mdc.issue.datavalidation.impl.event;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.impl.MessageSeeds;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class SuspectValueCreatedEvent extends DataValidationEvent {
    private final TimeService timeService;
    private final Clock clock;
    private final ValidationService validationService;
    private final JsonService jsonService;

    private ChannelsContainer channelsContainer;
    private Range<Instant> affectedRange;
    private List<ReadingQualityRecord> suspects;

    @Inject
    public SuspectValueCreatedEvent(Thesaurus thesaurus,
                                    MeteringService meteringService,
                                    DeviceService deviceService,
                                    IssueDataValidationService issueDataValidationService,
                                    IssueService issueService,
                                    TimeService timeService,
                                    Clock clock,
                                    ValidationService validationService,
                                    JsonService jsonService) {
        super(thesaurus, meteringService, deviceService, issueDataValidationService, issueService);
        this.timeService = timeService;
        this.clock = clock;
        this.validationService = validationService;
        this.jsonService = jsonService;
    }

    @Override
    void init(Map<?, ?> jsonPayload) {
        try {
            this.channelsContainer = meteringService.findChannelsContainer(((Number) jsonPayload.get("channelsContainerId")).longValue())
                    .orElse(null); // not possible but let it be
            this.affectedRange = ((Map<Number, Map<String, ?>>) jsonPayload.get("suspectedScope")).values().stream()
                    .map(this::toRange)
                    .reduce(Range::span)
                    .orElse(null); // not possible too
        } catch (Exception e) {
            throw new UnableToCreateIssueException(thesaurus, MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }

    private Range<Instant> toRange(Map<String, ?> rangeMap) {
        // TODO: refactor deserialization using something like jsonpath
        Instant lowerEndpoint = jsonService.deserialize(String.valueOf(rangeMap.get("lowerEndpoint")), Instant.class);
        Instant upperEndpoint = jsonService.deserialize(String.valueOf(rangeMap.get("upperEndpoint")), Instant.class);
        BoundType lowerBoundType = BoundType.valueOf(String.valueOf(rangeMap.get("lowerBoundType")));
        BoundType upperBoundType = BoundType.valueOf(String.valueOf(rangeMap.get("upperBoundType")));
        return Range.range(lowerEndpoint, lowerBoundType, upperEndpoint, upperBoundType);
    }

    @Override
    public void apply(Issue issue) {
        // Increase urgency of issue in case of repeating new suspects created event
        if (issue instanceof OpenIssueDataValidation) {
            OpenIssueDataValidation dataValidationIssue = (OpenIssueDataValidation) issue;
            dataValidationIssue.setPriority(Priority.get(issue.getPriority().increaseUrgency(), issue.getPriority().getImpact()));

            Instant last = Instant.MIN;
            if (suspects != null) {
                for (ReadingQualityRecord rqr : suspects) {
                    Instant current = rqr.getReadingTimestamp();
                    if (current.isAfter(last)) {
                        last = current;
                    }
                    dataValidationIssue.addNotEstimatedBlock(rqr.getChannel(), rqr.getReadingType(), rqr.getReadingTimestamp());
                }

                dataValidationIssue.setLastSuspectOccurrenceDatetime(last);
                dataValidationIssue.setTotalOccurrencesNumber(suspects.size());
            }
        }
    }

    @Override
    public boolean checkOccurrenceConditions(final String thresholdWithRelativePeriodString, final String validationRulesString) {
        if (validationRulesString.isEmpty()) {
            return false;
        }

        final List<String> thresholdWithRelativePeriod = parseRawInputToList(thresholdWithRelativePeriodString);
        if (thresholdWithRelativePeriod.size() != 2) {
            throw new UnableToCreateIssueException(thesaurus, MessageSeeds.COULD_NOT_PARSE_THRESHOLD_WITH_RELATIVE_PERIOD);
        }

        final int eventCountThreshold = Integer.parseInt(thresholdWithRelativePeriod.get(0));
        final Range<Instant> timeRange = getTimeRange(thresholdWithRelativePeriod.get(1));
        if (affectedRange == null || !timeRange.isConnected(affectedRange)) {
            return false;
        }

        final Set<Long> validationRuleIds = Arrays.stream(validationRulesString.split(";"))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toSet());
        final List<ValidationRule> validationRules = validationService.findValidationRules(validationRuleIds);
        suspects = findSuspects(timeRange, validationRules);
        return suspects.size() >= eventCountThreshold;
    }

    private Range<Instant> getTimeRange(final String relativePeriodId) {
        final Optional<RelativePeriod> relativePeriod = timeService.findRelativePeriod(Long.parseLong(relativePeriodId));
        if (!relativePeriod.isPresent()) {
            throw new UnableToCreateIssueException(thesaurus, MessageSeeds.COULD_NOT_FIND_RELATIVE_PERIOD, relativePeriodId);
        }
        return relativePeriod.get().getClosedInterval(ZonedDateTime.now(clock));
    }

    private List<String> parseRawInputToList(String rawInput) {
        return Arrays.stream(rawInput.split(":")).map(String::trim).collect(Collectors.toList());
    }

    private List<ReadingQualityRecord> findSuspects(final Range<Instant> timeRange, final List<ValidationRule> validationRules) {
        return Optional.ofNullable(channelsContainer)
                .flatMap(ChannelsContainer::getMeter)
                .map(meter -> findSuspects(meter, timeRange, validationRules))
                .orElseGet(Collections::emptyList);
    }

    private static List<ReadingQualityRecord> findSuspects(final Meter meter, final Range<Instant> timeRange, final List<ValidationRule> validationRules) {
        QualityCodeSystem expectedSystem = QualityCodeSystem.MDC;
        Set<ReadingQualityType> nonValidationCategoryTypes = new HashSet<>();
        Set<Integer> validationCategoryIndices = validationRules.stream()
                .map(ValidationRule::getReadingQualityType)
                .filter(type -> {
                    if (type.system().map(expectedSystem::equals).isPresent() && type.hasValidationCategory()) {
                        return true;
                    } else {
                        nonValidationCategoryTypes.add(type);
                        return false;
                    }
                })
                .map(ReadingQualityType::getIndexCode)
                .collect(Collectors.toSet());

        // we look for all suspects and all informative qualities of validation rules of interest
        ReadingQualityWithTypeFetcher fetcher = meter.findReadingQualities()
                .inTimeInterval(timeRange)
                .actual()
                .ofQualityIndex(QualityCodeIndex.SUSPECT);
        if (!validationCategoryIndices.isEmpty()) {
            fetcher = fetcher
                    .orOfAnotherType()
                    .ofQualitySystem(expectedSystem)
                    .ofQualityIndices(QualityCodeCategory.VALIDATION, validationCategoryIndices);
        }
        if (!nonValidationCategoryTypes.isEmpty()) {
            fetcher = fetcher
                    .orOfAnotherType()
                    .ofQualityTypes(nonValidationCategoryTypes);
        }
        return fetcher.stream()
                .collect(Collectors.groupingBy(rqr -> Pair.of(Pair.of(rqr.getChannel(), rqr.getReadingType()), rqr.getReadingTimestamp())))
                // grouped by particular timestamps on a cim channel.
                // both suspect & informative quality at one timestamp would mean that the suspect was created by one of the validation rules of interest
                .values()
                .stream()
                .filter(list -> list.size() > 1) // <= 1 means only suspect which is created by another rule, or only informative validation quality; not interesting for us
                .flatMap(list -> list.stream().filter(ReadingQualityRecord::isSuspect)) // take only suspect if we have it here, and just omit the informative qualities
                .collect(Collectors.toList());
    }

    @Override
    protected Optional<ChannelsContainer> findChannelsContainer() {
        return Optional.ofNullable(channelsContainer);
    }

    @Override
    protected Optional<Channel> findChannel() {
        throw new UnsupportedOperationException("Should not be used for " + SuspectValueCreatedEvent.class.getSimpleName());
    }

    @Override
    protected Optional<ReadingType> findReadingType() {
        throw new UnsupportedOperationException("Should not be used for " + SuspectValueCreatedEvent.class.getSimpleName());
    }
}
