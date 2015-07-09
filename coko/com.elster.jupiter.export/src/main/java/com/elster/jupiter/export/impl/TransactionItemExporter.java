package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.util.List;

class TransactionItemExporter implements ItemExporter {

    private final ItemExporter decorated;
    private final TransactionService transactionService;

    public TransactionItemExporter(TransactionService transactionService, ItemExporter decorated) {
        this.decorated = decorated;
        this.transactionService = transactionService;
    }

    @Override
    public List<FormattedExportData> exportItem(DataExportOccurrence occurrence, MeterReadingData meterReadingData) {
        try (TransactionContext context = transactionService.getContext()) {
            List<FormattedExportData> range = decorated.exportItem(occurrence, meterReadingData);
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
