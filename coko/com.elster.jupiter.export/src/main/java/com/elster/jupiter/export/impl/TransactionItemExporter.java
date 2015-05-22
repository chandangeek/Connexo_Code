package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.Range;

import java.time.Instant;

class TransactionItemExporter implements ItemExporter {

    private final ItemExporter decorated;
    private final TransactionService transactionService;

    public TransactionItemExporter(TransactionService transactionService, ItemExporter decorated) {
        this.decorated = decorated;
        this.transactionService = transactionService;
    }

    @Override
    public Range<Instant> exportItem(DataExportOccurrence occurrence, MeterReadingData meterReadingData) {
        try (TransactionContext context = transactionService.getContext()) {
            Range<Instant> range = decorated.exportItem(occurrence, meterReadingData);
            context.commit();
            return range;
        }
    }

    @Override
    public void done() {
        try (TransactionContext context = transactionService.getContext()) {
            decorated.done();
            context.commit();
        }
    }
}
