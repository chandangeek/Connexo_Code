/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

class LoggingItemExporter implements ItemExporter {

    private final ItemExporter decorated;
    private final Logger logger;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;
    private final DateTimeFormatter timeFormatter;

    public LoggingItemExporter(Thesaurus thesaurus,
                               TransactionService transactionService,
                               Logger logger,
                               ItemExporter decorated,
                               ThreadPrincipalService threadPrincipalService) {
        this.transactionService = transactionService;
        this.logger = logger;
        this.decorated = decorated;
        this.thesaurus = thesaurus;
        this.timeFormatter = getTimeFormatter(threadPrincipalService.getLocale());
    }

    @Override
    public List<FormattedExportData> exportItem(DataExportOccurrence occurrence, MeterReadingData meterReadingData) {
        ReadingTypeDataExportItem item = meterReadingData.getItem();
        String itemDescription = item.getDescription();
        try {
            Range<Instant> range = ((IExportTask) occurrence.getTask()).getReadingDataSelectorConfig().get().getStrategy().adjustedExportPeriod(occurrence, item);
            String fromDate = range.hasLowerBound() ? timeFormatter.format(range.lowerEndpoint()) : "";
            String toDate = range.hasUpperBound() ? timeFormatter.format(range.upperEndpoint()) : "";

            List<FormattedExportData> data = decorated.exportItem(occurrence, meterReadingData);

            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_EXPORTED_SUCCESFULLY.log(logger, thesaurus, itemDescription, fromDate, toDate)));
            return data;
        } catch (DataExportException e) {
            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_FAILED.log(logger, thesaurus, e.getCause(), itemDescription)));
            throw e;
        } catch (FatalDataExportException e) {
            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.ITEM_FATALLY_FAILED.log(logger, thesaurus, e.getCause(), itemDescription)));
            throw e;
        }
    }

    @Override
    public void done() {
        decorated.done();
    }

    private DateTimeFormatter getTimeFormatter(Locale locale) {
        return DefaultDateTimeFormatters.longDate(locale).withLongTime().build().withZone(ZoneId.systemDefault()).withLocale(locale);
    }
}
