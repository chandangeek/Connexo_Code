package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRule;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
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
    private ItemExporter decorated;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataExportOccurrence occurrence;
    @Mock
    private IReadingTypeDataExportItem item;
    @Mock
    private ReadingType readingType;
    @Mock
    private Meter meter;
    @Mock
    private NlsMessageFormat successFormat, failedFormat, fatallyFailedFormat;
    @Mock
    private MeterReadingData meterReadingData;
    @Mock
    private FormattedData formattedData;
    @Mock
    private ExportTask task;
    @Mock
    private IReadingTypeDataSelector dataSelector;

    public LoggingItemExporterTest() {
    }

    @Before
    public void setUp() {
        logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logRecorder = new LogRecorder(Level.ALL);
        logger.addHandler(logRecorder);

        from = ZonedDateTime.of(2013, 4, 18, 13, 2, 19, 0, ZoneId.systemDefault());
        to = ZonedDateTime.of(2013, 4, 18, 18, 2, 19, 0, ZoneId.systemDefault());
        range =  Range.closed(from.toInstant(), to.toInstant());

        when(task.getReadingTypeDataSelector()).thenReturn(Optional.of(dataSelector));
        when(meterReadingData.getItem()).thenReturn(item);
        when(decorated.exportItem(occurrence, meterReadingData)).thenReturn(Collections.emptyList());
        when(item.getReadingType()).thenReturn(readingType);
        when(readingType.getAliasName()).thenReturn("The Speed Of Pain");
        when(item.getReadingContainer()).thenReturn(meter);
        when(meter.getMeter(any())).thenReturn(Optional.of(meter));
        when(meter.getMRID()).thenReturn("MRID4711");
        when(thesaurus.getFormat(MessageSeeds.ITEM_EXPORTED_SUCCESFULLY)).thenReturn(successFormat);
        when(successFormat.format(anyVararg())).thenAnswer(invocation -> {
            return MessageFormat.format(MessageSeeds.ITEM_EXPORTED_SUCCESFULLY.getDefaultFormat(), invocation.getArguments()[0], invocation.getArguments()[1], invocation.getArguments()[2], invocation.getArguments()[3]);
        });
        when(thesaurus.getFormat(MessageSeeds.ITEM_FAILED)).thenReturn(failedFormat);
        when(failedFormat.format(anyVararg())).thenAnswer(invocation -> {
            return MessageFormat.format(MessageSeeds.ITEM_FAILED.getDefaultFormat(), invocation.getArguments()[0], invocation.getArguments()[1]);
        });
        when(thesaurus.getFormat(MessageSeeds.ITEM_FATALLY_FAILED)).thenReturn(fatallyFailedFormat);
        when(fatallyFailedFormat.format(anyVararg())).thenAnswer(invocation -> {
            return MessageFormat.format(MessageSeeds.ITEM_FATALLY_FAILED.getDefaultFormat(), invocation.getArguments()[0], invocation.getArguments()[1]);
        });
        when(occurrence.getTask()).thenReturn(task);
        when(dataSelector.adjustedExportPeriod(occurrence, item)).thenReturn(range);

        loggingItemExporter = new LoggingItemExporter(thesaurus, transactionService, logger, decorated);


    }

    @After
    public void tearDown() {

    }

    @Test
    public void testExportItem() throws Exception {

        loggingItemExporter.exportItem(occurrence, meterReadingData);

        assertThat(logRecorder.getRecords()).hasSize(1);

        LogRecord logRecord = logRecorder.getRecords().get(0);

        assertThat(logRecord.getLevel()).isEqualTo(Level.INFO);
        assertThat(logRecord.getMessage()).isEqualTo("Item MRID4711:The Speed Of Pain exported successfully for period Thu, 18 Apr 2013 13:02:19 NZST - Thu, 18 Apr 2013 18:02:19 NZST");
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
        assertThat(logRecord.getMessage()).isEqualTo("Item MRID4711:The Speed Of Pain failed to export");

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
        assertThat(logRecord.getMessage()).isEqualTo("Item MRID4711:The Speed Of Pain fatally failed to export");

    }
}