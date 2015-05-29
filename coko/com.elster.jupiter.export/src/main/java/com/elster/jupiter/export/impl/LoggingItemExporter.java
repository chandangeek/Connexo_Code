package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

class LoggingItemExporter implements ItemExporter {

    private final ItemExporter decorated;
    private final Logger logger;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final DateTimeFormatter timeFormatter = DefaultDateTimeFormatters.mediumDate().withLongTime().build().withZone(ZoneId.systemDefault());

    public LoggingItemExporter(Thesaurus thesaurus, TransactionService transactionService, Logger logger, ItemExporter decorated) {
        this.transactionService = transactionService;
        this.logger = logger;
        this.decorated = decorated;
        this.thesaurus = thesaurus;
    }

    @Override
    public Range<Instant> exportItem(DataExportOccurrence occurrence, MeterReadingData meterReadingData) {
        ReadingTypeDataExportItem item = meterReadingData.getItem();
        String mrid = item.getReadingContainer().getMeter(occurrence.getTriggerTime()).map(Meter::getMRID).orElse("");
        String readingType = item.getReadingType().getAliasName();
        try {
            Range<Instant> range = decorated.exportItem(occurrence, meterReadingData);
            String fromDate = range.hasLowerBound() ? timeFormatter.format(range.lowerEndpoint()) : "";
            String toDate = range.hasUpperBound() ? timeFormatter.format(range.upperEndpoint()) : "";
            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_EXPORTED_SUCCESFULLY.log(logger, thesaurus, mrid, readingType, fromDate, toDate)));
            return range;
        } catch (DataExportException e) {
            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_FAILED.log(logger, thesaurus, e.getCause(), mrid, readingType)));
            throw e;
        } catch (FatalDataExportException e) {
            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_FATALLY_FAILED.log(logger, thesaurus, e.getCause(), mrid, readingType)));
            throw e;
        }
    }

    @Override
    public void done() {
        decorated.done();
    }
}
