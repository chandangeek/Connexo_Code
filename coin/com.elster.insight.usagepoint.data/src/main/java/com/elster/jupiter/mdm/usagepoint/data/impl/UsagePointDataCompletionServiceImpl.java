package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataCompletionSummaryFlag;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataCompletionSummaryType;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataModificationSummaryFlags;
import com.elster.jupiter.mdm.usagepoint.data.IChannelDataCompletionSummary;
import com.elster.jupiter.mdm.usagepoint.data.IChannelDataCompletionSummaryFlag;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataCompletionService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.ValidChannelDataSummaryFlags;
import com.elster.jupiter.mdm.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

class UsagePointDataCompletionServiceImpl implements UsagePointDataCompletionService {
    private final Thesaurus thesaurus;
    private final ValidationService validationService;

    @Inject
    UsagePointDataCompletionServiceImpl(UsagePointDataModelService usagePointDataModelService,
                                        ValidationService validationService) {
        this.thesaurus = usagePointDataModelService.thesaurus();
        this.validationService = validationService;
    }

    /**
     * Gathers validation statistics by {@link ChannelDataCompletionSummaryFlag ChannelDataValidationSummaryFlags}
     * on a given {@code channel} within a given {@code interval}. Please note, there's no guarantee what happens
     * if the interval starts before channel creation.
     *
     * @param channel {@link Channel} to gather statistics for.
     * @param interval The time interval to gather statistics for.
     * @return {@link IChannelDataCompletionSummary}.
     */
    @Override
    public List<IChannelDataCompletionSummary> getDataCompletionStatistics(Channel channel, Range<Instant> interval) {
        List<IChannelDataCompletionSummary> summaryList = new ArrayList<>();
        TreeMap<Instant, Set<ReadingQualityType>> qualityTypesByAllTimings =
                (channel.isRegular() ? channel.toList(interval).stream() : channel.getReadings(interval)
                        .stream()
                        .map(BaseReading::getTimeStamp))
                        .collect(Collectors.toMap(
                                Function.identity(),
                                time -> Sets.newHashSet(),
                                (hashSet1, hashSet2) -> hashSet1, // merge is not needed, everything should be distinct already
                                TreeMap::new
                        ));
        ChannelDataCompletionSummaryImpl generalSummary = new ChannelDataCompletionSummaryImpl(interval, ChannelDataCompletionSummaryType.GENERAL);
        ChannelDataCompletionSummaryImpl editedSummary = new ChannelDataCompletionSummaryImpl(interval, ChannelDataCompletionSummaryType.EDITED);
        ChannelDataCompletionSummaryImpl validSummary = new ChannelDataCompletionSummaryImpl(interval, ChannelDataCompletionSummaryType.VALID);
        summaryList.add(generalSummary);
        channel.findReadingQualities() // supply the map with all qualities to consider
                .inTimeInterval(interval)
                .actual()
                .ofQualitySystem(QualityCodeSystem.MDM)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.KNOWNMISSINGREAD, QualityCodeIndex.ERRORCODE, QualityCodeIndex.ACCEPTED))
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED, QualityCodeCategory.ESTIMATED, QualityCodeCategory.VALIDATION))
                .stream()
                .collect(Collectors.toMap(
                        ReadingQualityRecord::getReadingTimestamp,
                        record -> Sets.newHashSet(record.getType()),
                        (set1, set2) -> {
                            set1.addAll(set2);
                            return set1;
                        },
                        () -> qualityTypesByAllTimings
                ));
        gatherEdited(qualityTypesByAllTimings, editedSummary);
        summaryList.add(editedSummary);
        Optional<Instant> lastCheckedOptional = validationService.getLastChecked(channel);
        int uncheckedTimingsCount;
        if (lastCheckedOptional.isPresent()) {
            Instant lastChecked = lastCheckedOptional.get();
            Map<Instant, Set<ReadingQualityType>> uncheckedTimings = qualityTypesByAllTimings.tailMap(lastChecked, false);
            uncheckedTimingsCount = uncheckedTimings.size();
            uncheckedTimings.clear(); // removed from qualityTypesByAllTimings
            if (Range.atMost(lastChecked).isConnected(interval)) { // something is validated
                gatherStatistics(qualityTypesByAllTimings, generalSummary);
                gatherValidated(qualityTypesByAllTimings, validSummary);
                if (validSummary.getSum() > 0) {
                    summaryList.add(validSummary);
                }
            }
        } else { // completely not validated
            uncheckedTimingsCount = qualityTypesByAllTimings.size();
        }
        accountFlagValue(generalSummary, ChannelDataCompletionSummaryFlag.NOT_VALIDATED, uncheckedTimingsCount);
        return summaryList;
    }

    private static void gatherEdited(Map<Instant, Set<ReadingQualityType>> qualityTypesByAllTimings,
                                     ChannelDataCompletionSummaryImpl summary) {
        Arrays.stream(ChannelDataModificationSummaryFlags.values()).forEach(flag ->
                accountFlagValue(summary, flag, (int) qualityTypesByAllTimings.entrySet().stream()
                        .filter(entry -> entry.getValue().stream().anyMatch(flag.getQualityTypePredicate()))
                        .map(Map.Entry::getKey)
                        .count()));
    }

    private static void gatherStatistics(Map<Instant, Set<ReadingQualityType>> qualityTypesByAllTimings,
                                       ChannelDataCompletionSummaryImpl summary) {
        accountFlagValue(summary, ChannelDataCompletionSummaryFlag.SUSPECT, (int) qualityTypesByAllTimings.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(ChannelDataCompletionSummaryFlag.SUSPECT.getQualityTypePredicate()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .stream()
                .peek(qualityTypesByAllTimings::remove)
                .count());

        accountFlagValue(summary, ChannelDataCompletionSummaryFlag.VALID, qualityTypesByAllTimings.size());
    }

    private static void gatherValidated(Map<Instant, Set<ReadingQualityType>> qualityTypesByAllTimings,
                                        ChannelDataCompletionSummaryImpl summary) {
        Arrays.stream(ValidChannelDataSummaryFlags.values()).forEach(flag ->
                accountFlagValue(summary, flag, (int) qualityTypesByAllTimings.entrySet().stream()
                        .filter(entry -> entry.getValue().stream().anyMatch(flag.getQualityTypePredicate()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList())
                        .stream()
                        .peek(qualityTypesByAllTimings::remove)
                        .count()));

        accountFlagValue(summary, ValidChannelDataSummaryFlags.VALID, qualityTypesByAllTimings.size());
    }

    private static void accountFlagValue(ChannelDataCompletionSummaryImpl summary,
                                         IChannelDataCompletionSummaryFlag flag, int value) {
        if (value > 0) {
            summary.incrementFlag(flag, value);
            summary.incrementOverallValue(value);
        }
    }

    @Override
    public Map<ReadingTypeDeliverable, List<IChannelDataCompletionSummary>> getDataCompletionStatistics(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration,
                                                                                                        MetrologyContract contract, Range<Instant> interval) {
        ChannelsContainer container = effectiveMetrologyConfiguration.getChannelsContainer(contract)
                .orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.METROLOGYCONTRACT_IS_NOT_LINKED_TO_USAGEPOINT,
                        contract.getId(), effectiveMetrologyConfiguration.getUsagePoint()
                        .getName()) {
                });
        Optional<Range<Instant>> optionalIntervalWithData = Optional.of(container)
                .map(Effectivity::getInterval)
                .map(Interval::toOpenClosedRange)
                .filter(interval::isConnected)
                .map(interval::intersection)
                .filter(not(Range::isEmpty));
        return contract.getDeliverables().stream().collect(Collectors.toMap(
                Function.identity(),
                deliverable -> optionalIntervalWithData
                        .map(intervalWithData -> container.getChannel(deliverable.getReadingType())
                                // channel cannot be unfound
                                .map(channel -> getDataCompletionStatistics(channel, intervalWithData))
                                .orElse(Collections.singletonList(getGeneralUsagePointDataCompletionSummary(intervalWithData))))
                        .orElse(Collections.singletonList(getGeneralUsagePointDataCompletionSummary(interval))),
                (summary1, summary2) -> { // merge should not appear since no ReadingTypeDeliverable duplication allowed
                    throw new LocalizedException(thesaurus,
                            MessageSeeds.DUPLICATE_READINGTYPE_ON_METROLOGY_CONTRACT,
                            contract.getId()) {
                    };
                },
                LinkedHashMap::new
        ));
    }

    @Override
    public IChannelDataCompletionSummary getGeneralUsagePointDataCompletionSummary (Range<Instant> interval) {
        return new ChannelDataCompletionSummaryImpl(interval, ChannelDataCompletionSummaryType.GENERAL);
    }
}
