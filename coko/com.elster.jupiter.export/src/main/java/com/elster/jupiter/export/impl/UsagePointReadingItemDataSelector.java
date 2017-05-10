/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
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

    private Map<UsagePoint, Boolean> isCompletData = new HashMap<>();
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
        }
        Set<Instant> instants = new TreeSet<>(item.getReadingContainer().toList(item.getReadingType(), exportInterval));
        return readings.stream()
                .filter(reading -> instants.contains(reading.getTimeStamp()))
                .collect(Collectors.toList());
    }

    @Override
    List<BaseReadingRecord> getReadingsUpdatedSince(IReadingTypeDataExportItem item, Range<Instant> exportInterval, Instant since) {
        List<BaseReadingRecord> readings;
        // data aggregation engine requires to wrap getReadings() call in transaction
        try (TransactionContext context = getTransactionService().getContext()) {
            readings = super.getReadingsUpdatedSince(item, exportInterval, since);
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
                if (!isCompletData.keySet().contains(item.getDomainObject())) {
                    item.getReadingContainer().getReadingTypes(exportInterval).stream()
                            .filter(readingType -> item.getSelector().getReadingTypes().contains(readingType))
                            .forEach(readingType -> {
                                Set<Instant> instants = new HashSet<>(item.getReadingContainer().toList(readingType, exportInterval));
                                try (TransactionContext context = getTransactionService().getContext()) {
                                    item.getReadingContainer().getReadings(exportInterval, readingType).stream()
                                            .map(BaseReadingRecord::getTimeStamp)
                                            .forEach(instants::remove);
                                }
                                if (!instants.isEmpty()) {
                                    isCompletData.putIfAbsent((UsagePoint) item.getDomainObject(), false);
                                }
                            });
                    isCompletData.putIfAbsent((UsagePoint) item.getDomainObject(), true);
                }
                return isCompletData.get(item.getDomainObject());
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
        if (!hasSuspectData.keySet().contains(item.getDomainObject())) {
            if (hasUnvalidatedReadings(item, readings) || item.getReadingContainer().getReadingTypes(interval).stream()
                    .filter(readingType -> item.getSelector().getReadingTypes().contains(readingType))
                    .anyMatch(readingType -> hasSuspects(item, readingType, interval))) {
                isCompletData.putIfAbsent((UsagePoint) item.getDomainObject(), true);
                logExportWindow(MessageSeeds.SUSPECT_WINDOW, interval, itemDescription);
                readings.clear();
            } else {
                isCompletData.putIfAbsent((UsagePoint) item.getDomainObject(), false);
            }
        } else {
            if (hasSuspectData.get(item.getDomainObject())) {
                logExportWindow(MessageSeeds.SUSPECT_WINDOW, interval, itemDescription);
                readings.clear();
            }
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
