package com.energyict.mdc.issue.datavalidation.impl.event;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.common.device.data.LoadProfileReading;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.impl.MessageSeeds;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuspectValueCreatedEvent extends DataValidationEvent {

    private Instant lastSuspectOccurrenceDatetime;
    private long totalOccurrencesNumber;

    private final TimeService timeService;
    private final Clock clock;

    private Instant readingTimeStamp;

    @Inject
    public SuspectValueCreatedEvent(Thesaurus thesaurus, MeteringService meteringService, DeviceService deviceService, IssueDataValidationService issueDataValidationService, IssueService issueService, TimeService timeService, Clock clock) {
        super(thesaurus, meteringService, deviceService, issueDataValidationService, issueService);
        this.timeService = timeService;
        this.clock = clock;
    }

    @Override
    void init(Map<?, ?> jsonPayload) {
        try {
            this.channelId = ((Number) jsonPayload.get("channelId")).longValue();
            this.readingType = (String) jsonPayload.get("readingType");
            this.readingTimeStamp = Instant.ofEpochMilli(((Number) jsonPayload.get("readingTimeStamp")).longValue());
        } catch (Exception e) {
            throw new UnableToCreateIssueException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }

    @Override
    public void apply(Issue issue) {
        // Increase urgency of issue in case of occuring new suspect created event
        if (issue instanceof OpenIssueDataValidation) {
            OpenIssueDataValidation dataValidationIssue = (OpenIssueDataValidation) issue;
            dataValidationIssue.setPriority(Priority.get(issue.getPriority().increaseUrgency(), issue.getPriority().getImpact()));

            if (Objects.isNull(dataValidationIssue.getLastSuspectOccurrenceDatetime())) {
                dataValidationIssue.setLastSuspectOccurrenceDatetime(lastSuspectOccurrenceDatetime);
            }

            if (dataValidationIssue.getTotalOccurrencesNumber() == 0) {
                dataValidationIssue.setTotalOccurrencesNumber(totalOccurrencesNumber);
            }

            Channel channel = findChannel().get();
            ReadingType readingType = findReadingType().get();
            channel.getCimChannel(readingType).get()
                    .findReadingQualities()
                    .ofQualitySystem(QualityCodeSystem.MDC)
                    .ofQualityIndex(QualityCodeIndex.SUSPECT)
                    .inTimeInterval(Range.all())
                    .actual()
                    .collect()
                    .forEach(rq -> dataValidationIssue.addNotEstimatedBlock(channel, readingType, rq.getReadingTimestamp()));
        }
    }

    public boolean checkValidationRule(final String relativePeriodWithCount, final String validationRules) {
        if (validationRules.isEmpty()) {
            return false;
        }

        final Range<Instant> timeRange = getTimeRange(relativePeriodWithCount);

        final Set<Long> validationRulesId = Arrays.stream(validationRules.split(";"))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toSet());

        ReadingType readingType = findReadingType().get();
        Channel coreChannel = findChannel().get();
        boolean needBulkReadingQualities = !coreChannel.getMainReadingType().equals(readingType);

        final List<Map<com.energyict.mdc.common.device.data.Channel, DataValidationStatus>> validationStates = getDevice().getChannels().stream()
                .filter(channel -> coreChannel.getReadingTypes().contains(channel.getReadingType()))
                .map(channel -> channel.getChannelData(timeRange))
                .flatMap(Collection::stream)
                .map(LoadProfileReading::getChannelValidationStates)
                .filter(states -> !states.isEmpty())
                .collect(Collectors.toList());

        final List<Map.Entry<com.energyict.mdc.common.device.data.Channel, DataValidationStatus>> validationStatesFilteredByReading = validationStates.stream()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .filter(this::containsReadingQualityRecord)
                .collect(Collectors.toList());

        if (validationStatesFilteredByReading.isEmpty()) {
            return false;
        }

        final List<ValidationRule> offendedValidationRules = validationStatesFilteredByReading.stream()
                .map(channelDataValidationStatusEntry -> {
                    if (needBulkReadingQualities) {
                        return channelDataValidationStatusEntry.getValue().getBulkOffendedRules();
                    } else {
                        return channelDataValidationStatusEntry.getValue().getOffendedRules();
                    }
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return !offendedValidationRules.isEmpty()
                && validationRulesId.containsAll(offendedValidationRules.stream().map(ValidationRule::getId).collect(Collectors.toList()));

    }

    private boolean containsReadingQualityRecord(final Map.Entry<com.energyict.mdc.common.device.data.Channel, DataValidationStatus> entry) {
        return Stream.concat(entry.getValue().getReadingQualities().stream(), entry.getValue().getBulkReadingQualities().stream())
                .map(o -> (ReadingQualityRecord) o)
                .anyMatch(readingQualityRecord -> readingQualityRecord.getReadingTimestamp().equals(readingTimeStamp));
    }

    @Override
    public boolean checkOccurrenceConditions(final String relativePeriodWithCount) {
        final List<String> relativePeriodWithCountValues = parseRawInputToList(relativePeriodWithCount);
        final int eventCountThreshold = Integer.parseInt(relativePeriodWithCountValues.get(0));
        final Range<Instant> timeRange = getTimeRange(relativePeriodWithCount);
        final List<ReadingQualityRecord> suspects = findSuspects(timeRange);

        totalOccurrencesNumber = suspects.size();

        if (totalOccurrencesNumber < eventCountThreshold) {
            return false;
        }

        final ReadingQualityRecord issueCreationCausingSuspect = suspects.get(eventCountThreshold - 1);
        lastSuspectOccurrenceDatetime = issueCreationCausingSuspect.getReadingTimestamp();

        return true;
    }

    private Range<Instant> getTimeRange(final String relativePeriodWithCount) {
        final List<String> relativePeriodWithCountValues = parseRawInputToList(relativePeriodWithCount);
        final Optional<RelativePeriod> relativePeriod = timeService.findRelativePeriod(Long.parseLong(relativePeriodWithCountValues.get(1)));

        if (relativePeriodWithCountValues.size() != 2) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS,
                    "Relative period with occurrence count for data validation issues",
                    String.valueOf(2),
                    String.valueOf(relativePeriodWithCountValues.size()));
        }

        if (!relativePeriod.isPresent()) {
            throw new LocalizedFieldValidationException(MessageSeeds.EVENT_BAD_DATA_NO_RELATIVE_PERIOD,
                    "Relative period can not be obtained! Relative Period: ",
                    relativePeriodWithCountValues.get(1));
        }

        return relativePeriod.get().getClosedInterval(clock.instant().atZone(clock.getZone()).with(LocalTime.now(clock)));
    }

    private List<String> parseRawInputToList(String rawInput) {
        return Arrays.stream(rawInput.split(":")).map(String::trim).collect(Collectors.toList());
    }

    private List<ReadingQualityRecord> findSuspects(final Range<Instant> timeRange) {
        // FIXME! CXO-12226
        return findChannelsContainer()
                .map(channelsContainer -> channelsContainer.getChannels().stream()
                        .flatMap(channel -> channel.findReadingQualities()
                                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                                .inTimeInterval(timeRange)
                                .stream())
                        .sorted(this::sortSuspectsByDateAscOrder)
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    private int sortSuspectsByDateAscOrder(final ReadingQualityRecord first, final ReadingQualityRecord second) {
        return first.getReadingTimestamp().compareTo(second.getReadingTimestamp());
    }
}
