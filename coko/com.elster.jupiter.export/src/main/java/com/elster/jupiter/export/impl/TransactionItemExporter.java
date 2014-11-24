package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.Range;

import java.time.Instant;

public class TransactionItemExporter implements ItemExporter {

    private final ItemExporter decorated;
    private final TransactionService transactionService;

    public TransactionItemExporter(TransactionService transactionService, ItemExporter decorated) {
        this.decorated = decorated;
        this.transactionService = transactionService;
    }

    @Override
    public Range<Instant> exportItem(DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        try (TransactionContext context = transactionService.getContext()) {
            Range<Instant> range = decorated.exportItem(occurrence, item);
            context.commit();
            return range;
        }
    }
}
