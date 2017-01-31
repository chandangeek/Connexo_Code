/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.MeterReadingValidationData;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.ExtraCollectors.toImmutableRangeSet;

class MeterReadingItemDataSelector extends AbstractItemDataSelector {

    @Inject
    MeterReadingItemDataSelector(Clock clock,
                                 ValidationService validationService,
                                 Thesaurus thesaurus,
                                 TransactionService transactionService,
                                 ThreadPrincipalService threadPrincipalService) {
        super(clock, validationService, thesaurus, transactionService, threadPrincipalService);
    }

    @Override
    Set<QualityCodeSystem> getQualityCodeSystems() {
        return ImmutableSet.of(QualityCodeSystem.MDC);
    }

    @Override
    public Optional<MeterReadingData> selectDataForUpdate(DataExportOccurrence occurrence, IReadingTypeDataExportItem item, Instant since) {
        if (!isExportUpdates(occurrence)) {
            return Optional.empty();
        }
        Range<Instant> updateInterval = determineUpdateInterval(occurrence, item);
        List<? extends BaseReadingRecord> readings = new ArrayList<>(item.getReadingContainer()
                .getReadingsUpdatedSince(updateInterval, item.getReadingType(), since));

        String itemDescription = item.getDescription();

        Optional<RelativePeriod> updateWindow = item.getSelector().getStrategy().getUpdateWindow();
        if (updateWindow.isPresent()) {
            RelativePeriod window = updateWindow.get();
            RangeSet<Instant> rangeSet = readings.stream()
                    .map(baseReadingRecord -> window.getOpenClosedInterval(
                            ZonedDateTime.ofInstant(baseReadingRecord.getTimeStamp(), item.getReadingContainer().getZoneId())))
                    .collect(toImmutableRangeSet());
            readings = rangeSet.asRanges().stream()
                    .flatMap(range -> {
                        List<? extends BaseReadingRecord> found = getReadings(item, range);
                        if (getExportStrategy(occurrence).get().isExportCompleteData()) {
                            handleValidatedDataOption(item, item.getSelector().getStrategy(), found, range, itemDescription);
                            if (!isComplete(item, range, found)) {
                                return Stream.empty();
                            }
                        }
                        return found.stream();
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (!readings.isEmpty()) {
            MeterReadingImpl meterReading = asMeterReading(item, readings);
            MeterReadingValidationData meterReadingValidationData = getValidationData(item, readings, updateInterval);
            return Optional.of(new MeterReadingData(item, meterReading, meterReadingValidationData, structureMarkerForUpdate()));
        }
        return Optional.empty();
    }

    private boolean isExportUpdates(DataExportOccurrence occurrence) {
        return getExportStrategy(occurrence).map(DataExportStrategy::isExportUpdate).orElse(false);
    }

    private Range<Instant> determineUpdateInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        TreeRangeSet<Instant> base = TreeRangeSet.create();
        Range<Instant> baseRange = determineBaseUpdateInterval(occurrence, item);
        base.add(baseRange);
        base.remove(((DefaultSelectorOccurrence) occurrence).getExportedDataInterval());
        return base.asRanges().stream().findFirst().orElse(baseRange);
    }

    private Range<Instant> determineBaseUpdateInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        return getExportStrategy(occurrence)
                .filter(DataExportStrategy::isExportUpdate)
                .flatMap(DataExportStrategy::getUpdatePeriod)
                .map(relativePeriod -> relativePeriod.getOpenClosedInterval(
                        ZonedDateTime.ofInstant(occurrence.getTriggerTime(), item.getReadingContainer().getZoneId())))
                .orElse(null);
    }

    private StructureMarker structureMarkerForUpdate() {
        return DefaultStructureMarker.createRoot(getClock(), "update");
    }

    private Optional<DataExportStrategy> getExportStrategy(DataExportOccurrence dataExportOccurrence) {
        IExportTask exportTask = (IExportTask) dataExportOccurrence.getTask();
        return exportTask.getReadingDataSelectorConfig().map(ReadingDataSelectorConfig::getStrategy);
    }
}
