package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.DefaultStructureMarker;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.tasks.TaskLogHandler;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionContext;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.elster.jupiter.devtools.tests.Matcher.matches;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataExportTaskExecutorTest {

    @Rule
    public TestRule timeZone = Using.timeZoneOfMcMurdo();

    private ZonedDateTime exportPeriodStart;
    private ZonedDateTime exportPeriodEnd;
    private ZonedDateTime triggerTime;
    private ZonedDateTime lastExported;
    private Range<Instant> exportPeriod;
    private LogRecorder logRecorder;
    private TransactionVerifier transactionService;

    @Mock
    private TaskService taskService;
    @Mock
    private IDataExportService dataExportService;
    @Mock
    private TaskOccurrence occurrence;
    @Mock
    private IDataExportOccurrence dataExportOccurrence;
    @Mock
    private IExportTask task;
    @Mock
    private EndDeviceGroup group;
    @Mock
    private EndDeviceMembership endDeviceMembership1, endDeviceMembership2;
    @Mock
    private Meter meter1, meter2, meter3;
    @Mock
    private IReadingTypeDataExportItem existingItem, newItem, obsoleteItem;
    @Mock
    private ReadingType readingType1, readingType2;
    @Mock
    private DataProcessorFactory dataProcessorFactory;
    @Mock
    private DataExportProperty dataExportProperty;
    @Mock
    private DataProcessor dataProcessor;
    @Mock
    private TaskLogHandler taskLogHandler;
    @Mock
    private DataExportStrategy strategy;
    @Mock(extraInterfaces = {IntervalReadingRecord.class})
    private Reading reading1, reading2;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Thesaurus thesaurus;
    @Mock
    private ReadingContainer readingContainer;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private IReadingTypeDataSelector readingTypeDataSelector;
    @Mock
    private MeteringService meteringService;
    @Mock
    private DataModel dataModel;
    @Mock
    private RelativePeriod exportRelativePeriod;

    public static final Predicate<IntervalReading> READING_1 = r -> r.getSource().equals("reading1");
    public static final Predicate<IntervalReading> READING_2 = r -> r.getSource().equals("reading2");

    @Before
    public void setUp() {
        exportPeriodStart = ZonedDateTime.of(2012, 11, 10, 6, 0, 0, 0, ZoneId.systemDefault());
        lastExported = ZonedDateTime.of(2012, 11, 5, 6, 0, 0, 0, ZoneId.systemDefault());
        exportPeriodEnd = ZonedDateTime.of(2012, 11, 11, 6, 0, 0, 0, ZoneId.systemDefault());
        triggerTime = ZonedDateTime.of(2012, 11, 20, 0, 0, 0, 0, ZoneId.systemDefault());
        exportPeriod = Range.openClosed(exportPeriodStart.toInstant(), exportPeriodEnd.toInstant());
        logRecorder = new LogRecorder(Level.ALL);

        transactionService = new TransactionVerifier(dataProcessor, newItem, existingItem);

        when(readingTypeDataSelector.getEndDeviceGroup()).thenReturn(group);
        when(readingTypeDataSelector.getReadingTypes()).thenReturn(ImmutableSet.of(readingType1));
        when(readingTypeDataSelector.addExportItem(meter1, readingType1)).thenReturn(newItem);
        when(task.getReadingTypeDataSelector()).thenReturn(Optional.of(readingTypeDataSelector));
        when(occurrence.createTaskLogHandler()).thenReturn(taskLogHandler);
        when(taskLogHandler.asHandler()).thenReturn(logRecorder);
        when(dataExportService.createExportOccurrence(occurrence)).thenReturn(dataExportOccurrence);
        when(dataExportService.findDataExportOccurrence(occurrence)).thenReturn(Optional.of(dataExportOccurrence));
        when(dataExportService.getDataProcessorFactory("CSV")).thenReturn(Optional.of(dataProcessorFactory));
        when(dataExportService.getDataSelectorFactory(DataExportService.STANDARD_DATA_SELECTOR)).thenReturn(Optional.of(new StandardDataSelectorFactory(transactionService, meteringService, thesaurus)));
        when(dataExportOccurrence.getTask()).thenReturn(task);
        when(dataExportOccurrence.getExportedDataInterval()).thenReturn(exportPeriod);
        when(dataExportOccurrence.getTriggerTime()).thenReturn(triggerTime.toInstant());
        when(task.getDataFormatter()).thenReturn("CSV");
        when(task.getDataSelector()).thenReturn(DataExportService.STANDARD_DATA_SELECTOR);
        when(task.getDataExportProperties()).thenReturn(Arrays.asList(dataExportProperty));
        when(readingTypeDataSelector.getStrategy()).thenReturn(strategy);
        when(dataExportProperty.getName()).thenReturn("name");
        when(dataExportProperty.getValue()).thenReturn("CSV");
        when(meter1.is(meter1)).thenReturn(true);
        when(meter2.is(meter2)).thenReturn(true);
        when(meter3.is(meter3)).thenReturn(true);
        when(meter1.getMeter(any())).thenReturn(Optional.of(meter1));
        when(meter2.getMeter(any())).thenReturn(Optional.of(meter2));
        when(meter3.getMeter(any())).thenReturn(Optional.of(meter3));
        doReturn(Arrays.asList(existingItem, obsoleteItem)).when(readingTypeDataSelector).getExportItems();
        when(existingItem.getReadingType()).thenReturn(readingType1);
        when(existingItem.getReadingContainer()).thenReturn(meter2);
        when(meter2.getMeter(any())).thenReturn(Optional.of(meter2));
        when(meter2.getUsagePoint(any())).thenReturn(Optional.<UsagePoint>empty());
        when(existingItem.getLastExportedDate()).thenReturn(Optional.of(lastExported.toInstant()));
        when(newItem.getLastExportedDate()).thenReturn(Optional.<Instant>empty());
        when(newItem.getReadingContainer()).thenReturn(meter1);
        when(meter1.getMeter(any())).thenReturn(Optional.of(meter1));
        when(meter1.getUsagePoint(any())).thenReturn(Optional.<UsagePoint>empty());
        when(newItem.getReadingType()).thenReturn(readingType1);
        when(obsoleteItem.getReadingType()).thenReturn(readingType1);
        when(obsoleteItem.getReadingContainer()).thenReturn(meter3);
        when(meter3.getMeter(any())).thenReturn(Optional.of(meter3));
        when(meter3.getUsagePoint(any())).thenReturn(Optional.<UsagePoint>empty());
        when(group.getMembers(exportPeriod)).thenReturn(Arrays.asList(endDeviceMembership1, endDeviceMembership2));
        when(endDeviceMembership1.getEndDevice()).thenReturn(meter1);
        when(endDeviceMembership2.getEndDevice()).thenReturn(meter2);
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(dataExportProperty.getName(), dataExportProperty.getValue());
        when(dataProcessorFactory.createDataFormatter(propertyMap)).thenReturn(dataProcessor);
        when(dataProcessorFactory.getPropertySpec("name")).thenReturn(propertySpec);
        when(strategy.isExportContinuousData()).thenReturn(false);
        doReturn(Arrays.asList(reading1)).when(meter1).getReadings(exportPeriod, readingType1);
        doReturn(Arrays.asList(reading2)).when(meter2).getReadings(exportPeriod, readingType1);
        when(dataProcessor.processData(any())).thenReturn(Optional.of(exportPeriodEnd.toInstant()));
        when(reading1.getSource()).thenReturn("reading1");
        when(reading2.getSource()).thenReturn("reading2");
        MeterReadingImpl meterReading1 = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock1 = IntervalBlockImpl.of("");
        intervalBlock1.addIntervalReading((IntervalReading) reading1);
        meterReading1.addIntervalBlock(intervalBlock1);
        MeterReadingData newItemData = new MeterReadingData(this.newItem, meterReading1, DefaultStructureMarker.createRoot("newItem"));
        MeterReadingImpl meterReading2 = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock2 = IntervalBlockImpl.of("");
        intervalBlock2.addIntervalReading((IntervalReading) reading1);
        meterReading1.addIntervalBlock(intervalBlock2);
        MeterReadingData existItemData = new MeterReadingData(this.existingItem, meterReading2, DefaultStructureMarker.createRoot("newItem"));
        when(readingTypeDataSelector.selectData(dataExportOccurrence)).thenReturn(Arrays.<ExportData>asList(newItemData, existItemData).stream());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testExecuteObsoleteItemIsDeactivated() {
        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        InOrder inOrder = inOrder(obsoleteItem);
        inOrder.verify(obsoleteItem).deactivate();
        inOrder.verify(obsoleteItem).update();
    }

    @Test
    public void testExecuteExistingItemIsUpdated() {
        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        InOrder inOrder = inOrder(existingItem);
        inOrder.verify(existingItem).activate();
        inOrder.verify(existingItem).setLastRun(triggerTime.toInstant());
        inOrder.verify(existingItem).setLastExportedDate(exportPeriodEnd.toInstant());
        inOrder.verify(existingItem).update();
    }

    @Test
    public void testNewItemIsUpdated() {
        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        InOrder inOrder = inOrder(newItem);
        inOrder.verify(newItem).activate();
        inOrder.verify(newItem).setLastRun(triggerTime.toInstant());
        inOrder.verify(newItem).setLastExportedDate(exportPeriodEnd.toInstant());
        inOrder.verify(newItem).update();
    }

    @Test
    public void testDataProcessorGetsTheRightNotifications() {
        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        ArgumentCaptor<Logger> logCaptor = ArgumentCaptor.forClass(Logger.class);
        ArgumentCaptor<MeterReadingData> readingCaptor1 = ArgumentCaptor.forClass(MeterReadingData.class);
        ArgumentCaptor<MeterReadingData> readingCaptor2 = ArgumentCaptor.forClass(MeterReadingData.class);

        InOrder inOrder = inOrder(dataProcessor);
        inOrder.verify(dataProcessor).startExport(eq(dataExportOccurrence), logCaptor.capture());
        inOrder.verify(dataProcessor).startItem(newItem);
        inOrder.verify(dataProcessor).processData(readingCaptor1.capture());
        inOrder.verify(dataProcessor).endItem(newItem);
        inOrder.verify(dataProcessor).startItem(existingItem);
        inOrder.verify(dataProcessor).processData(readingCaptor2.capture());
        inOrder.verify(dataProcessor).endItem(existingItem);
        inOrder.verify(dataProcessor).endExport();

        logCaptor.getValue().log(Level.WARNING, "testHandler");
        assertThat(logRecorder.getRecords()).hasSize(3);
        LogRecord logRecord = logRecorder.getRecords().get(2);
        assertThat(logRecord.getLevel()).isEqualTo(Level.WARNING);
        assertThat(logRecord.getMessage()).isEqualTo("testHandler");

        assertThat(readingCaptor1.getValue().getMeterReading().getReadings()).has(new ReadingFor(reading1));
        assertThat(readingCaptor2.getValue().getMeterReading().getReadings()).has(new ReadingFor(reading2));
    }

    @Test
    public void testDataProcessorGetsTheRightNotificationsForIntervalReadings() {
        when(readingType1.isRegular()).thenReturn(true);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        ArgumentCaptor<Logger> logCaptor = ArgumentCaptor.forClass(Logger.class);
        ArgumentCaptor<MeterReadingData> readingCaptor1 = ArgumentCaptor.forClass(MeterReadingData.class);
        ArgumentCaptor<MeterReadingData> readingCaptor2 = ArgumentCaptor.forClass(MeterReadingData.class);

        InOrder inOrder = inOrder(dataProcessor);
        inOrder.verify(dataProcessor).startExport(eq(dataExportOccurrence), logCaptor.capture());
        inOrder.verify(dataProcessor).startItem(newItem);
        inOrder.verify(dataProcessor).processData(readingCaptor1.capture());
        inOrder.verify(dataProcessor).endItem(newItem);
        inOrder.verify(dataProcessor).startItem(existingItem);
        inOrder.verify(dataProcessor).processData(readingCaptor2.capture());
        inOrder.verify(dataProcessor).endItem(existingItem);
        inOrder.verify(dataProcessor).endExport();

        logCaptor.getValue().log(Level.WARNING, "testHandler");
        assertThat(logRecorder.getRecords()).hasSize(3);
        LogRecord logRecord = logRecorder.getRecords().get(2);
        assertThat(logRecord.getLevel()).isEqualTo(Level.WARNING);
        assertThat(logRecord.getMessage()).isEqualTo("testHandler");

        assertThat(readingCaptor1.getValue().getMeterReading().getIntervalBlocks()).hasSize(1);
        assertThat(readingCaptor1.getValue().getMeterReading().getIntervalBlocks().get(0).getIntervals()).has(new IntervalReadingFor(reading1));
        assertThat(readingCaptor2.getValue().getMeterReading().getIntervalBlocks()).hasSize(1);
        assertThat(readingCaptor2.getValue().getMeterReading().getIntervalBlocks().get(0).getIntervals()).has(new IntervalReadingFor(reading2));
    }

    @Test
    public void testDataProcessorGetsTheRightNotificationsInTheRightTransactions() {
        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        verify(dataProcessor, transactionService.notInTransaction()).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor, transactionService.inTransaction(2)).startItem(newItem);
        verify(dataProcessor, transactionService.inTransaction(2)).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));
        verify(dataProcessor, transactionService.inTransaction(2)).endItem(newItem);
        verify(dataProcessor, transactionService.inTransaction(4)).startItem(existingItem);
        verify(dataProcessor, transactionService.inTransaction(4)).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));
        verify(dataProcessor, transactionService.inTransaction(4)).endItem(existingItem);
        verify(dataProcessor, transactionService.notInTransaction()).endExport();

        verify(newItem, transactionService.inTransaction(6)).update();
        verify(existingItem, transactionService.inTransaction(6)).update();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasCommitted();
        transactionService.assertThatTransaction(5).wasCommitted();
    }

    @Test
    public void testStartExportThrowsFatalException() {
        doThrow(new FatalDataExportException(new RuntimeException())).when(dataProcessor).startExport(eq(dataExportOccurrence), any());

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor, never()).startItem(newItem);
        verify(dataProcessor, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().contains(reading1))));
        verify(dataProcessor, never()).endItem(newItem);
        verify(dataProcessor, never()).startItem(existingItem);
        verify(dataProcessor, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().contains(reading2))));
        verify(dataProcessor, never()).endItem(existingItem);
        verify(dataProcessor, never()).endExport();

    }

    @Test
    public void testStartExportThrowsRuntimeException() {
        doThrow(new RuntimeException()).when(dataProcessor).startExport(eq(dataExportOccurrence), any());

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor, never()).startItem(newItem);
        verify(dataProcessor, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().contains(reading1))));
        verify(dataProcessor, never()).endItem(newItem);
        verify(dataProcessor, never()).startItem(existingItem);
        verify(dataProcessor, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().contains(reading2))));
        verify(dataProcessor, never()).endItem(existingItem);
        verify(dataProcessor, never()).endExport();

    }

    @Test
    public void testStartItemThrowsFatalException() {
        doThrow(new FatalDataExportException(new RuntimeException())).when(dataProcessor).startItem(existingItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor).startItem(newItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));
        verify(dataProcessor).endItem(newItem);
        verify(dataProcessor).startItem(existingItem);
        verify(dataProcessor, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));
        verify(dataProcessor, never()).endItem(existingItem);
        verify(dataProcessor, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted(); // newItem
        transactionService.assertThatTransaction(3).wasCommitted(); // log success of newItem
        transactionService.assertThatTransaction(4).wasNotCommitted(); // existingItem
        transactionService.assertThatTransaction(5).wasCommitted(); // log failure of existingItem

    }

    @Test
    public void testStartItemThrowsRuntimeException() {
        doThrow(new RuntimeException()).when(dataProcessor).startItem(existingItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor).startItem(newItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));
        verify(dataProcessor).endItem(newItem);
        verify(dataProcessor).startItem(existingItem);
        verify(dataProcessor, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));
        verify(dataProcessor, never()).endItem(existingItem);
        verify(dataProcessor, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasNotCommitted();

    }

    @Test
    public void testStartItemThrowsDataExportException() {
        doThrow(DataExportException.class).when(dataProcessor).startItem(newItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor).startItem(newItem);
        verify(dataProcessor, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));
        verify(dataProcessor, never()).endItem(newItem);
        verify(dataProcessor).startItem(existingItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));
        verify(dataProcessor).endItem(existingItem);
        verify(dataProcessor).endExport();

        transactionService.assertThatTransaction(2).wasNotCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
    }

    @Test
    public void testProcessItemThrowsFatalException() {
        doThrow(new FatalDataExportException(new RuntimeException())).when(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor).startItem(newItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));
        verify(dataProcessor).endItem(newItem);
        verify(dataProcessor).startItem(existingItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));
        verify(dataProcessor, never()).endItem(existingItem);
        verify(dataProcessor, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasNotCommitted();
    }

    @Test
    public void testProcessItemThrowsRuntimeException() {
        doThrow(new RuntimeException()).when(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor).startItem(newItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));
        verify(dataProcessor).endItem(newItem);
        verify(dataProcessor).startItem(existingItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));
        verify(dataProcessor, never()).endItem(existingItem);
        verify(dataProcessor, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasNotCommitted();
        transactionService.assertThatTransaction(5).wasCommitted();
    }

    @Test
    public void testProcessItemThrowsDataExportException() {
        doThrow(DataExportException.class).when(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor).startItem(newItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));
        verify(dataProcessor, never()).endItem(newItem);
        verify(dataProcessor).startItem(existingItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));
        verify(dataProcessor).endItem(existingItem);
        verify(dataProcessor).endExport();

        transactionService.assertThatTransaction(2).wasNotCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
    }

    @Test
    public void testEndItemThrowsFatalException() {
        doThrow(new FatalDataExportException(new RuntimeException())).when(dataProcessor).endItem(existingItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor).startItem(newItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));
        verify(dataProcessor).endItem(newItem);
        verify(dataProcessor).startItem(existingItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));
        verify(dataProcessor).endItem(existingItem);
        verify(dataProcessor, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasNotCommitted();
        transactionService.assertThatTransaction(5).wasCommitted();
    }

    @Test
    public void testEndItemThrowsRuntimeException() {
        doThrow(new RuntimeException()).when(dataProcessor).endItem(existingItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor).startItem(newItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));
        verify(dataProcessor).endItem(newItem);
        verify(dataProcessor).startItem(existingItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));
        verify(dataProcessor).endItem(existingItem);
        verify(dataProcessor, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasNotCommitted();
        transactionService.assertThatTransaction(5).wasCommitted();
    }

    @Test
    public void testEndItemThrowsDataExportException() {
        doThrow(DataExportException.class).when(dataProcessor).endItem(newItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        verify(dataProcessor).startExport(eq(dataExportOccurrence), any());
        verify(dataProcessor).startItem(newItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading1")))));
        verify(dataProcessor).endItem(newItem);
        verify(dataProcessor).startItem(existingItem);
        verify(dataProcessor).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().stream().anyMatch(rd -> rd.getSource().equals("reading2")))));
        verify(dataProcessor).endItem(existingItem);
        verify(dataProcessor).endExport();

        transactionService.assertThatTransaction(2).wasNotCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
    }

    private static class IntervalReadingFor extends Condition<List<? extends IntervalReading>> {
        private final IntervalReading intervalReading;

        private IntervalReadingFor(Object intervalReading) {
            this.intervalReading = (IntervalReading) intervalReading;
        }

        @Override
        public boolean matches(List<? extends IntervalReading> intervalReadings) {
            return intervalReadings.stream().anyMatch(r -> r.getSource().equals(intervalReading.getSource()));
        }
    }

    private static class ReadingFor extends Condition<List<? extends Reading>> {
        private final Reading reading;

        private ReadingFor(Reading reading) {
            this.reading = reading;
        }

        @Override
        public boolean matches(List<? extends Reading> intervalReadings) {
            return intervalReadings.stream().anyMatch(r -> r.getSource().equals(reading.getSource()));
        }
    }
}