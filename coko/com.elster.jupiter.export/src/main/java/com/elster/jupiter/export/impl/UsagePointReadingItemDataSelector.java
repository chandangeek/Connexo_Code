/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

class UsagePointReadingItemDataSelector extends AbstractItemDataSelector {

    private Map<UsagePoint, Boolean> isCompleteData = new HashMap<>();
    private Map<UsagePoint, Boolean> hasSuspectData = new HashMap<>();

    @Inject
    UsagePointReadingItemDataSelector(Clock clock,
                                      ValidationService validationService,
                                      Thesaurus thesaurus,
                                      TransactionService transactionService,
                                      ThreadPrincipalService threadPrincipalService) {
        super(clock, validationService, thesaurus, transactionService, threadPrincipalService);
    }

    @Override
    List<BaseReadingRecord> getReadings(IReadingTypeDataExportItem item, Range<Instant> exportInterval) {
        List<BaseReadingRecord> readings;
        // data aggregation engine requires to wrap getReadings() call in transaction
        try (TransactionContext context = getTransactionService().getContext()) {
            readings = super.getReadings(item, exportInterval);
            context.commit();
        }
        Set<Instant> instants = new TreeSet<>(item.getReadingContainer().toList(item.getReadingType(), exportInterval));
        return item.getReadingType().isRegular() ? readings.stream()
                .filter(reading -> instants.contains(reading.getTimeStamp()))
                .collect(Collectors.toList()) : readings;
    }

    @Override
    List<BaseReadingRecord> getReadingsUpdatedSince(IReadingTypeDataExportItem item, Range<Instant> exportInterval, Instant since) {
        List<BaseReadingRecord> readings;
        // data aggregation engine requires to wrap getReadings() call in transaction
        try (TransactionContext context = getTransactionService().getContext()) {
            readings = super.getReadingsUpdatedSince(item, exportInterval, since);
            context.commit();
        }
        return readings;
    }

    @Override
    Set<QualityCodeSystem> getQualityCodeSystems() {
        return ImmutableSet.of(QualityCodeSystem.MDM);
    }

    @Override
    boolean isComplete(IReadingTypeDataExportItem item, Range<Instant> exportInterval, List<? extends BaseReadingRecord> readings) {
        switch (item.getSelector().getStrategy().getMissingDataOption()) {
            case EXCLUDE_OBJECT:
                if (!isCompleteData.containsKey(item.getDomainObject())) {
                    boolean hasMissing = ((UsagePoint) item.getDomainObject()).getEffectiveMetrologyConfigurations(exportInterval).stream()
                            .anyMatch(metrologyConfiguration ->
                                    metrologyConfiguration.getMetrologyConfiguration().getContracts().stream()
                                            .filter(contract -> ((UsagePointReadingSelectorConfig) item.getSelector()).getMetrologyPurpose()
                                                    .map(purpose -> purpose.equals(contract.getMetrologyPurpose()))
                                                    .orElse(true))
                                            .anyMatch(contract -> metrologyConfiguration.getChannelsContainer(contract).map(
                                                    channelsContainer -> contract.getDeliverables().stream().map(ReadingTypeDeliverable::getReadingType)
                                                            .filter(readingType -> item.getSelector().getReadingTypes().contains(readingType))
                                                            .anyMatch(readingType -> hasMissings(channelsContainer, exportInterval, readingType))
                                                    ).orElse(false)
                                            )
                            );
                    isCompleteData.putIfAbsent((UsagePoint) item.getDomainObject(), !hasMissing);
                }
                return isCompleteData.get(item.getDomainObject());
            default:
                Set<Instant> instants = new HashSet<>(item.getReadingContainer().toList(item.getReadingType(), exportInterval));
                readings.stream()
                        .map(BaseReadingRecord::getTimeStamp)
                        .forEach(instants::remove);
                return instants.isEmpty();
        }
    }

    @Override
    void handleExcludeObject(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings, Range<Instant> interval, String itemDescription) {
        if (!hasSuspectData.containsKey(item.getDomainObject())) {
            if (hasUnvalidatedReadings(item, readings) || ((UsagePoint) item.getDomainObject()).getEffectiveMetrologyConfigurations(interval).stream()
                    .anyMatch(metrologyConfiguration ->
                            metrologyConfiguration.getMetrologyConfiguration().getContracts().stream()
                                    .filter(contract -> ((UsagePointReadingSelectorConfig) item.getSelector()).getMetrologyPurpose()
                                            .map(purpose -> purpose.equals(contract.getMetrologyPurpose()))
                                            .orElse(true))
                                    .anyMatch(contract -> metrologyConfiguration.getChannelsContainer(contract).map(
                                            channelsContainer -> contract.getDeliverables().stream().map(ReadingTypeDeliverable::getReadingType)
                                                    .filter(readingType -> item.getSelector().getReadingTypes().contains(readingType))
                                                    .anyMatch(readingType -> hasSuspects(channelsContainer, readingType, interval))
                                            ).orElse(false)
                                    )
                    )) {
                hasSuspectData.putIfAbsent((UsagePoint) item.getDomainObject(), true);
                logExportWindow(MessageSeeds.SUSPECT_WINDOW, interval, itemDescription);
                readings.clear();
            } else {
                hasSuspectData.putIfAbsent((UsagePoint) item.getDomainObject(), false);
            }
        } else {
            if (hasSuspectData.get(item.getDomainObject())) {
                logExportWindow(MessageSeeds.SUSPECT_WINDOW, interval, itemDescription);
                readings.clear();
            }
        }
    }

    private boolean hasMissings(ChannelsContainer channelsContainer, Range<Instant> exportInterval, ReadingType readingType) {
        Range<Instant> channelContainerRange = channelsContainer.getInterval().toOpenClosedRange();
        if (exportInterval.isConnected(channelContainerRange)) {
            Range<Instant> actualRange = exportInterval.intersection(channelContainerRange);
            Set<Instant> instants = new HashSet<>(channelsContainer.toList(readingType, actualRange));
            try (TransactionContext context = getTransactionService().getContext()) {
                channelsContainer.getReadings(actualRange, readingType).stream()
                        .map(BaseReadingRecord::getTimeStamp)
                        .forEach(instants::remove);
                context.commit();
            }
            if (!instants.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSuspects(ChannelsContainer channelsContainer, ReadingType readingType, Range<Instant> exportInterval) {
        Range<Instant> channelContainerRange = channelsContainer.getInterval().toOpenClosedRange();
        if (exportInterval.isConnected(channelContainerRange)) {
            Range<Instant> actualRange = exportInterval.intersection(channelContainerRange);
            return channelsContainer
                    .getReadingQualities(getQualityCodeSystems(), QualityCodeIndex.SUSPECT, readingType, actualRange).stream()
                    .map(ReadingQualityRecord::getReadingTimestamp)
                    .findAny()
                    .isPresent();
        }
        return false;
    }
}
