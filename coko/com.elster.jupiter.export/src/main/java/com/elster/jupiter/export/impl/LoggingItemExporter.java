package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.logging.Logger;

class LoggingItemExporter implements ItemExporter {

    private final ItemExporter decorated;
    private final Logger logger;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;

    public LoggingItemExporter(Thesaurus thesaurus, TransactionService transactionService, Logger logger, ItemExporter decorated) {
        this.transactionService = transactionService;
        this.logger = logger;
        this.decorated = decorated;
        this.thesaurus = thesaurus;
    }

    @Override
    public Range<Instant> exportItem(DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        try {
            Range<Instant> range = decorated.exportItem(occurrence, item);
            MessageSeeds.ITEM_EXPORTED_SUCCESFULLY.log(logger, thesaurus, item, range);
            return range;
        } catch (DataExportException e) {
            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_FAILED.log(logger, thesaurus, e.getCause(), item)));
            throw e;
        } catch (FatalDataExportException e) {
            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_FATALLY_FAILED.log(logger, thesaurus, e.getCause(), item)));
            throw e;
        }
    }
}
