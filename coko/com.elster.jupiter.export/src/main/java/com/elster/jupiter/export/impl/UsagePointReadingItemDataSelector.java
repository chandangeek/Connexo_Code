package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class UsagePointReadingItemDataSelector extends AbstractItemDataSelector {

    @Inject
    UsagePointReadingItemDataSelector(Clock clock, ValidationService validationService, Thesaurus thesaurus, TransactionService transactionService) {
        super(clock, validationService, thesaurus, transactionService);
    }

    @Override
    public Optional<MeterReadingData> selectDataForUpdate(DataExportOccurrence occurrence, IReadingTypeDataExportItem item, Instant since) {
        // not supported yet
        return Optional.empty();
    }

    @Override
    List<BaseReadingRecord> getReadings(IReadingTypeDataExportItem item, Range<Instant> exportInterval) {
        // data aggregation engine requires to wrap getReadings() call in transaction
        try (TransactionContext context = getTransactionService().getContext()) {
            return super.getReadings(item, exportInterval);
        }
    }

    @Override
    Set<QualityCodeSystem> getQualityCodeSystems() {
        return ImmutableSet.of(QualityCodeSystem.MDM);
    }
}
