/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class LoggingItemExporterTest {

    @Rule
    public TestRule southOfDownUnder = Using.timeZoneOfMcMurdo();
    @Rule
    public TestRule statesSide = Using.locale("en", "US");
    @Rule
    public TestRule mockito = new MockitoJUnitRule(this);

    private LoggingItemExporter loggingItemExporter;
    private Logger logger;
    private LogRecorder logRecorder;

    private TransactionService transactionService = new TransactionVerifier();
    private ZonedDateTime from;
    private ZonedDateTime to;
    private Range<Instant> range;

    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private ItemExporter decorated;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private DataExportOccurrence occurrence;
    @Mock
    private ReadingTypeDataExportItem item;
    @Mock
    private Meter meter;
    @Mock
    private MeterReadingData meterReadingData;
    @Mock
    private IExportTask task;
    @Mock
    private ReadingDataSelectorConfigImpl readingDataSelectorConfig;
    @Mock
    private DataExportStrategy dataExportStrategy;

    @Before
    public void setUp() {
        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logRecorder = new LogRecorder(Level.ALL);
        logger.addHandler(logRecorder);

        from = ZonedDateTime.of(2013, 4, 18, 13, 2, 19, 0, ZoneId.systemDefault());
        to = ZonedDateTime.of(2013, 4, 18, 18, 2, 19, 0, ZoneId.systemDefault());
        range = Range.closed(from.toInstant(), to.toInstant());

        when(meterReadingData.getItem()).thenReturn(item);
        when(decorated.exportItem(occurrence, meterReadingData)).thenReturn(Collections.emptyList());
        when(item.getDescription()).thenReturn("I'm Marilyn and I take drugs");
        when(item.getReadingContainer()).thenReturn(meter);
        when(meter.getMeter(any())).thenReturn(Optional.of(meter));

        when(occurrence.getTask()).thenReturn(task);
        when(task.getReadingDataSelectorConfig()).thenReturn(Optional.of(readingDataSelectorConfig));
        when(readingDataSelectorConfig.getStrategy()).thenReturn(dataExportStrategy);
        when(dataExportStrategy.adjustedExportPeriod(occurrence, item)).thenReturn(range);
        when(threadPrincipalService.getLocale()).thenReturn(Locale.US);

        loggingItemExporter = new LoggingItemExporter(thesaurus, transactionService, logger, decorated, threadPrincipalService);
    }

    @Test
    public void testExportItem() throws Exception {
        loggingItemExporter.exportItem(occurrence, meterReadingData);

        assertThat(logRecorder.getRecords()).hasSize(1);

        LogRecord logRecord = logRecorder.getRecords().get(0);

        assertThat(logRecord.getLevel()).isEqualTo(Level.INFO);
        assertThat(logRecord.getMessage()).isEqualTo("Item I'm Marilyn and I take drugs exported successfully for period Thu, Apr-18-'13 01:02:19 PM - Thu, Apr-18-'13 06:02:19 PM");
    }

    @Test
    public void testExportItemFails() throws Exception {
        doThrow(DataExportException.class).when(decorated).exportItem(occurrence, meterReadingData);

        try {
            loggingItemExporter.exportItem(occurrence, meterReadingData);
        } catch (Exception e) {
            //expected
        }

        assertThat(logRecorder.getRecords()).hasSize(1);

        LogRecord logRecord = logRecorder.getRecords().get(0);

        assertThat(logRecord.getLevel()).isEqualTo(Level.WARNING);
        assertThat(logRecord.getMessage()).isEqualTo("Item I'm Marilyn and I take drugs failed to export");
    }

    @Test
    public void testExportItemFailsFatally() throws Exception {
        doThrow(FatalDataExportException.class).when(decorated).exportItem(occurrence, meterReadingData);

        try {
            loggingItemExporter.exportItem(occurrence, meterReadingData);
        } catch (Exception e) {
            //expected
        }

        assertThat(logRecorder.getRecords()).hasSize(1);

        LogRecord logRecord = logRecorder.getRecords().get(0);

        assertThat(logRecord.getLevel()).isEqualTo(Level.SEVERE);
        assertThat(logRecord.getMessage()).isEqualTo("Item I'm Marilyn and I take drugs fatally failed to export");
    }
}
