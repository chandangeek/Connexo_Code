/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

class UsagePointReadingItemDataSelector extends AbstractItemDataSelector {

    @Inject
    UsagePointReadingItemDataSelector(Clock clock,
                                      ValidationService validationService,
                                      Thesaurus thesaurus,
                                      TransactionService transactionService,
                                      ThreadPrincipalService threadPrincipalService) {
        super(clock, validationService, thesaurus, transactionService, threadPrincipalService);
    }

    @Override
    public Optional<MeterReadingData> selectDataForUpdate(DataExportOccurrence occurrence, IReadingTypeDataExportItem item, Instant since) {
        // not supported yet
        return Optional.empty();
    }

    @Override
    List<BaseReadingRecord> getReadings(IReadingTypeDataExportItem item, Range<Instant> exportInterval) {
        List<BaseReadingRecord> readings;
        // data aggregation engine requires to wrap getReadings() call in transaction
        try (TransactionContext context = getTransactionService().getContext()) {
            readings = super.getReadings(item, exportInterval);
        }
        Set<Instant> instants = new TreeSet<>(item.getReadingContainer().toList(item.getReadingType(), exportInterval));
        return readings.stream()
                .filter(reading -> instants.contains(reading.getTimeStamp()))
                .collect(Collectors.toList());
    }

    @Override
    Set<QualityCodeSystem> getQualityCodeSystems() {
        return ImmutableSet.of(QualityCodeSystem.MDM);
    }

    @Override
    boolean isComplete(IReadingTypeDataExportItem item, Range<Instant> exportInterval, List<? extends BaseReadingRecord> readings) {
        switch (item.getSelector().getStrategy().getMissingDataOption()) {
            case EXCLUDE_OBJECT:
                final boolean[] isComplete = {true};
                    item.getReadingContainer().getReadingTypes(exportInterval)
                            .forEach(readingType -> {
                                Set<Instant> instants = new HashSet<>(item.getReadingContainer().toList(readingType, exportInterval));
                                item.getReadingContainer().getReadings(exportInterval, readingType).stream()
                                        .map(BaseReadingRecord::getTimeStamp)
                                        .forEach(instants::remove);
                                if (!instants.isEmpty()) {
                                    isComplete[0] = false;
                                }
                            });
                return isComplete[0];
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
        if (hasUnvalidatedReadings(item, readings) || item.getReadingContainer().getReadingTypes(interval).stream()
                .anyMatch(readingType -> hasSuspects(item, readingType, interval))) {
            logExportWindow(MessageSeeds.SUSPECT_WINDOW, interval, itemDescription);
            readings.clear();
        }
    }

    private boolean hasSuspects(IReadingTypeDataExportItem item, ReadingType readingType, Range<Instant> interval) {
        return item.getReadingContainer()
                .getReadingQualities(getQualityCodeSystems(), QualityCodeIndex.SUSPECT, readingType, interval).stream()
                .map(ReadingQualityRecord::getReadingTimestamp)
                .findAny()
                .isPresent();
    }
}
