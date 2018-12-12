/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.elster.jupiter.mdm.usagepoint.data.ValidChannelDataSummaryFlags;
import com.elster.jupiter.mdm.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class UsagePointDataCompletionServiceImpl implements UsagePointDataCompletionService {
    private final Thesaurus thesaurus;
    private final ValidationService validationService;

    UsagePointDataCompletionServiceImpl(Thesaurus thesaurus, ValidationService validationService) {
        this.thesaurus = thesaurus;
        this.validationService = validationService;
    }

    /**
     * Gathers validation statistics by {@link ChannelDataCompletionSummaryFlag ChannelDataValidationSummaryFlags}
     * on a given {@code readingType} on a given {@code usagePoint} within a given {@code interval}. Please note, there's no guarantee what happens
     * if the interval starts before channel creation.
     *
     * @param usagePoint {@link UsagePoint} to gather statistics for.
     * @param metrologyPurpose {@link MetrologyPurpose} to gather statistics for.
     * @param readingType {@link ReadingType} to gather statistics for.
     * @param interval The time interval to gather statistics for.
     * @return {@link IChannelDataCompletionSummary}.
     */
    @Override
    public List<IChannelDataCompletionSummary> getDataCompletionStatistics(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose, Range<Instant> interval, ReadingType readingType) {
        List<IChannelDataCompletionSummary> summaryList = new LinkedList<>();
        TreeMap<Instant, Set<ReadingQualityType>> qualityTypesByAllTimings = getReadingTimestamps(usagePoint, metrologyPurpose, interval, readingType)
                .collect(Collectors.toMap(
                                Function.identity(),
                                time -> Sets.newHashSet(),
                                (hashSet1, hashSet2) -> hashSet1, // merge is not needed, everything should be distinct already
                                TreeMap::new
                        ));
        ChannelDataCompletionSummaryImpl generalSummary = new ChannelDataCompletionSummaryImpl(interval, ChannelDataCompletionSummaryType.GENERAL);
        ChannelDataCompletionSummaryImpl editedSummary = new ChannelDataCompletionSummaryImpl(interval, ChannelDataCompletionSummaryType.EDITED);
        ChannelDataCompletionSummaryImpl validSummary = new ChannelDataCompletionSummaryImpl(interval, ChannelDataCompletionSummaryType.VALID);
        ChannelDataCompletionSummaryImpl estimatedSummary = new ChannelDataCompletionSummaryImpl(interval, ChannelDataCompletionSummaryType.ESTIMATED);
        summaryList.add(generalSummary);
        getReadingQualities(usagePoint, metrologyPurpose, interval, readingType)
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
        summaryList.add(estimatedSummary);
        Optional<Instant> lastCheckedOptional = getLastChecked(usagePoint, metrologyPurpose, readingType);
        int uncheckedTimingsCount;
        if (lastCheckedOptional.isPresent()) {
            Instant lastChecked = lastCheckedOptional.get();
            Map<Instant, Set<ReadingQualityType>> uncheckedTimings = qualityTypesByAllTimings.tailMap(lastChecked, false);
            uncheckedTimingsCount = uncheckedTimings.size();
            uncheckedTimings.clear(); // removed from qualityTypesByAllTimings
            if (Range.atMost(lastChecked).isConnected(interval)) { // something is validated
                gatherEstimated(qualityTypesByAllTimings, estimatedSummary);
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
        int projectedCount = 0;
        for(ChannelDataModificationSummaryFlags flag: ChannelDataModificationSummaryFlags.values()) {
            List<Instant> modificationKeys = qualityTypesByAllTimings.entrySet().stream()
                    .filter(entry -> entry.getValue().stream().anyMatch(flag.getQualityTypePredicate()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            int tempProjectedCount = (int) modificationKeys.stream()
                    .filter(key -> qualityTypesByAllTimings.get(key).stream().anyMatch(ReadingQualityType::hasProjectedCategory))
                    .count();
            int modificationCount = modificationKeys.size() - tempProjectedCount;
            accountFlagValue(summary, flag, modificationCount);
            projectedCount += tempProjectedCount;
        }
        accountFlagValue(summary, ValidChannelDataSummaryFlags.PROJECTED, projectedCount);
    }

    private static void gatherEstimated(Map<Instant, Set<ReadingQualityType>> qualityTypesByAllTimings,
                                        ChannelDataCompletionSummaryImpl summary) {
        List<Instant> keysWithEstimatedCategory = qualityTypesByAllTimings.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(ReadingQualityType::hasEstimatedCategory))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        int projectedCount = (int) keysWithEstimatedCategory.stream()
                .filter(key -> qualityTypesByAllTimings.get(key).stream().anyMatch(ReadingQualityType::hasProjectedCategory))
                .count();
        int estimatedCount = keysWithEstimatedCategory.size() - projectedCount;

        accountFlagValue(summary, ChannelDataCompletionSummaryType.ESTIMATED, estimatedCount);
        accountFlagValue(summary, ValidChannelDataSummaryFlags.PROJECTED, projectedCount);
    }

    private static void gatherStatistics(Map<Instant, Set<ReadingQualityType>> qualityTypesByAllTimings, ChannelDataCompletionSummaryImpl summary) {
        accountFlagValue(summary, ChannelDataCompletionSummaryFlag.SUSPECT, (int) qualityTypesByAllTimings.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(ChannelDataCompletionSummaryFlag.SUSPECT.getQualityTypePredicate()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .stream()
                .peek(qualityTypesByAllTimings::remove)
                .count());

        accountFlagValue(summary, ChannelDataCompletionSummaryFlag.VALID, qualityTypesByAllTimings.size());
    }

    private static void gatherValidated(Map<Instant, Set<ReadingQualityType>> qualityTypesByAllTimings, ChannelDataCompletionSummaryImpl summary) {
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

    private static void accountFlagValue(ChannelDataCompletionSummaryImpl summary, IChannelDataCompletionSummaryFlag flag, int value) {
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

        return contract.getDeliverables().stream().collect(Collectors.toMap(
                Function.identity(),
                deliverable -> container.getChannel(deliverable.getReadingType())
                        // channel cannot be unfound
                        .map(channel -> getDataCompletionStatistics(effectiveMetrologyConfiguration.getUsagePoint(), contract.getMetrologyPurpose(), interval, channel.getMainReadingType()))
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
    public IChannelDataCompletionSummary getGeneralUsagePointDataCompletionSummary(Range<Instant> interval) {
        return new ChannelDataCompletionSummaryImpl(interval, ChannelDataCompletionSummaryType.GENERAL);
    }

    private Stream<Instant> getReadingTimestamps(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose, Range<Instant> interval, ReadingType readingType) {
        return getChannels(usagePoint, metrologyPurpose, interval, readingType)
                .flatMap(channel -> Ranges.nonEmptyIntersection(channel.getChannelsContainer().getInterval().toOpenClosedRange(), interval)
                        .map(range -> channel.isRegular()
                                ? channel.toList(range).stream()
                                : channel.getReadings(range).stream().map(BaseReading::getTimeStamp))
                        .orElse(Stream.empty()));
    }

    private Stream<ReadingQualityRecord> getReadingQualities(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose, Range<Instant> interval, ReadingType readingType) {
        return getChannels(usagePoint, metrologyPurpose, interval, readingType)
                .flatMap(channel -> channel.findReadingQualities() // supply the map with all qualities to consider
                        .inTimeInterval(Ranges.copy(channel.getChannelsContainer().getRange().intersection(interval)).asOpenClosed())
                        .actual()
                        .ofQualitySystem(QualityCodeSystem.MDM)
                        .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.KNOWNMISSINGREAD, QualityCodeIndex.ERRORCODE, QualityCodeIndex.ACCEPTED))
                        .orOfAnotherTypeInSameSystems()
                        .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED, QualityCodeCategory.ESTIMATED, QualityCodeCategory.VALIDATION, QualityCodeCategory.PROJECTED))
                        .stream());
    }

    private Stream<Channel> getChannels(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose, Range<Instant> interval, ReadingType readingType) {
        return usagePoint.getEffectiveMetrologyConfigurations().stream()
                .map(emc -> getMetrologyContract(emc.getMetrologyConfiguration(), metrologyPurpose).flatMap(emc::getChannelsContainer))
                .flatMap(Functions.asStream())
                .filter(channelsContainer -> channelsContainer.getInterval().toOpenClosedRange().isConnected(interval))
                .map(channelsContainer -> channelsContainer.getChannel(readingType))
                .flatMap(Functions.asStream());
    }

    @Override
    public Optional<Instant> getLastChecked(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose) {
        Map<ChannelsContainer, Instant> lastCheskedMap = usagePoint.getEffectiveMetrologyConfigurations().stream()
                .map(emc -> getMetrologyContract(emc.getMetrologyConfiguration(), metrologyPurpose).flatMap(emc::getChannelsContainer))
                .flatMap(Functions.asStream())
                .map(channelsContainer -> Pair.of(channelsContainer, validationService.getLastChecked(channelsContainer)))
                .filter(pair -> pair.getLast().isPresent())
                .collect(Collectors.toMap(Pair::getFirst, p -> p.getLast().get(), (a, b) -> a));

        return Optional.ofNullable(getMinLastCheckedFromIncompleteRanges(lastCheskedMap).orElse(getMaxLastChecked(lastCheskedMap).orElse(null)));
    }

    private Optional<MetrologyContract> getMetrologyContract(MetrologyConfiguration metrologyConfiguration, MetrologyPurpose purpose){
        return metrologyConfiguration.getContracts().stream().filter(metrologyContract -> metrologyContract.getMetrologyPurpose().equals(purpose)).findFirst();
    }

    @Override
    public Optional<Instant> getLastChecked(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose, ReadingType readingType) {
        ValidationEvaluator validationEvaluator = validationService.getEvaluator();
        Map<ChannelsContainer, Instant> lastCheskedMap = usagePoint.getEffectiveMetrologyConfigurations().stream()
                .map(emc -> getMetrologyContract(emc.getMetrologyConfiguration(), metrologyPurpose).flatMap(emc::getChannelsContainer))
                .flatMap(Functions.asStream())
                .map(channelsContainer -> Pair.of(channelsContainer, validationEvaluator.getLastChecked(channelsContainer, readingType)))
                .filter(pair -> pair.getLast().isPresent())
                .collect(Collectors.toMap(Pair::getFirst, p -> p.getLast().get(), (a, b) -> a));

        return Optional.ofNullable(getMinLastCheckedFromIncompleteRanges(lastCheskedMap).orElseGet(() -> getMaxLastChecked(lastCheskedMap).orElse(null)));
    }

    private Optional<Instant> getMinLastCheckedFromIncompleteRanges(Map<ChannelsContainer, Instant> lastCheckedMap) {
        return lastCheckedMap.entrySet().stream()
                .filter(e -> !e.getKey().getRange().hasUpperBound() || e.getValue().isBefore(e.getKey().getRange().upperEndpoint()))
                .map(Map.Entry::getValue)
                .min(Comparator.naturalOrder());
    }

    private Optional<Instant> getMaxLastChecked(Map<ChannelsContainer, Instant> lastCheskedMap){
        return lastCheskedMap.values().stream().max(Comparator.naturalOrder());
    }
}
