/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummary;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryFlag;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataCompletionService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
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
import java.util.Arrays;
import java.util.LinkedHashMap;
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
     * Gathers validation statistics by {@link ChannelDataValidationSummaryFlag ChannelDataValidationSummaryFlags}
     * on a given {@code channel} within a given {@code interval}. Please note, there's no guarantee what happens
     * if the interval starts before channel creation.
     *
     * @param channel {@link Channel} to gather statistics for.
     * @param interval The time interval to gather statistics for.
     * @return {@link ChannelDataValidationSummary}.
     */
    @Override
    public ChannelDataValidationSummary getValidationSummary(Channel channel, Range<Instant> interval) {
        ReadingQualityType valid = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.DATAVALID);
        TreeMap<Instant, Set<ReadingQualityType>> qualityTypesByAllTimings =
                (channel.isRegular() ? channel.toList(interval).stream() : channel.getReadings(interval)
                        .stream()
                        .map(BaseReading::getTimeStamp))
                        .collect(Collectors.toMap(
                                Function.identity(),
                                time -> Sets.newHashSet(valid), // this quality is to be checked after readings with all other qualities are gone,
                                // so now should be added per all timestamps
                                (hashSet1, hashSet2) -> hashSet1, // merge is not needed, everything should be distinct already
                                TreeMap::new
                        ));
        Optional<Instant> lastCheckedOptional = validationService.getLastChecked(channel);
        int uncheckedTimingsCount;
        ChannelDataValidationSummaryImpl summary = new ChannelDataValidationSummaryImpl(interval);
        if (lastCheckedOptional.isPresent()) {
            Instant lastChecked = lastCheckedOptional.get();
            Range<Instant> checked = Range.atMost(lastChecked);
            Map<Instant, Set<ReadingQualityType>> uncheckedTimings = qualityTypesByAllTimings.tailMap(lastChecked, false);
            uncheckedTimingsCount = uncheckedTimings.size();
            uncheckedTimings.clear(); // removed from qualityTypesByAllTimings
            if (checked.isConnected(interval)) { // something is validated
                channel.findReadingQualities() // supply the map with all other qualities to consider
                        .inTimeInterval(checked.intersection(interval))
                        .actual()
                        .ofQualitySystem(QualityCodeSystem.MDM)
                        .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.KNOWNMISSINGREAD, QualityCodeIndex.ERRORCODE))
                        .orOfAnotherTypeInSameSystems()
                        .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED, QualityCodeCategory.ESTIMATED))
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
                gatherStatistics(qualityTypesByAllTimings, summary);
            }
        } else { // completely not validated
            uncheckedTimingsCount = qualityTypesByAllTimings.size();
        }
        accountFlagValue(summary, ChannelDataValidationSummaryFlag.NOT_VALIDATED, uncheckedTimingsCount);
        return summary;
    }

    private static void gatherStatistics(Map<Instant, Set<ReadingQualityType>> qualityTypesByAllTimings,
                                         ChannelDataValidationSummaryImpl summary) {
        Arrays.stream(ChannelDataValidationSummaryFlag.values()).forEach(flag ->
                accountFlagValue(summary, flag, (int) qualityTypesByAllTimings.entrySet().stream()
                        .filter(entry -> entry.getValue().stream().anyMatch(flag.getQualityTypePredicate()))
                        .map(Map.Entry::getKey)
                        // need to collect instants in a new collection so that they can be removed from current one
                        .collect(Collectors.toList())
                        .stream()
                        .peek(qualityTypesByAllTimings::remove)
                        .count()));
    }

    private static void accountFlagValue(ChannelDataValidationSummaryImpl summary,
                                         ChannelDataValidationSummaryFlag flag, int value) {
        if (value > 0) {
            summary.incrementFlag(flag, value);
            summary.incrementOverallValue(value);
        }
    }

    @Override
    public Map<ReadingTypeDeliverable, ChannelDataValidationSummary> getValidationSummary(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration,
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
                                .map(channel -> getValidationSummary(channel, intervalWithData))
                                .orElse(new ChannelDataValidationSummaryImpl(intervalWithData)))
                        .orElse(new ChannelDataValidationSummaryImpl(interval)),
                (summary1, summary2) -> { // merge should not appear since no ReadingTypeDeliverable duplication allowed
                    throw new LocalizedException(thesaurus,
                            MessageSeeds.DUPLICATE_READINGTYPE_ON_METROLOGY_CONTRACT,
                            contract.getId()) {
                    };
                },
                LinkedHashMap::new
        ));
    }
}
