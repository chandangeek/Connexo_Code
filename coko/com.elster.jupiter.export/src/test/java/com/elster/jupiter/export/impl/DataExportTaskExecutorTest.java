package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingRecord;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.devtools.tests.Matcher.matches;
import static com.elster.jupiter.export.impl.IntervalReadingImpl.intervalReading;
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
    private Clock clock = Clock.system(ZoneId.systemDefault());

    private List<List<ExportData>> passedStreams = new ArrayList<>(); // streams wil be collected to list

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
    private DataFormatterFactory dataFormatterFactory;
    @Mock
    private DataExportProperty dataExportProperty;
    @Mock
    private DataFormatter dataFormatter;
    @Mock
    private TaskLogHandler taskLogHandler;
    @Mock
    private DataExportStrategy strategy;
    @Mock(extraInterfaces = {IntervalReadingRecord.class})
    private ReadingRecord reading1, reading2;
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
    @Mock
    private FormattedData formattedData;
    @Mock
    private Destination destination;

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

        transactionService = new TransactionVerifier(dataFormatter, newItem, existingItem);

        when(formattedData.lastExported()).thenReturn(Optional.of(lastExported.toInstant()));
        when(readingTypeDataSelector.getEndDeviceGroup()).thenReturn(group);
        when(readingTypeDataSelector.getReadingTypes()).thenReturn(ImmutableSet.of(readingType1));
        when(readingTypeDataSelector.addExportItem(meter1, readingType1)).thenReturn(newItem);
        when(task.getReadingTypeDataSelector()).thenReturn(Optional.of(readingTypeDataSelector));
        when(occurrence.createTaskLogHandler()).thenReturn(taskLogHandler);
        when(taskLogHandler.asHandler()).thenReturn(logRecorder);
        when(dataExportService.createExportOccurrence(occurrence)).thenReturn(dataExportOccurrence);
        when(dataExportService.findDataExportOccurrence(occurrence)).thenReturn(Optional.of(dataExportOccurrence));
        when(dataExportService.getDataFormatterFactory("CSV")).thenReturn(Optional.of(dataFormatterFactory));
        when(dataExportService.getDataSelectorFactory(DataExportService.STANDARD_DATA_SELECTOR)).thenReturn(Optional.of(new StandardDataSelectorFactory(transactionService, meteringService, thesaurus)));
        when(dataExportOccurrence.getTask()).thenReturn(task);
        when(dataExportOccurrence.getExportedDataInterval()).thenReturn(exportPeriod);
        when(dataExportOccurrence.getTriggerTime()).thenReturn(triggerTime.toInstant());
        when(task.getDataFormatter()).thenReturn("CSV");
        when(task.getDataSelector()).thenReturn(DataExportService.STANDARD_DATA_SELECTOR);
        when(task.getDataExportProperties()).thenReturn(Arrays.asList(dataExportProperty));
        when(task.getCompositeDestination()).thenReturn(destination);
        when(task.hasDefaultSelector()).thenReturn(true);
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
        when(dataFormatterFactory.createDataFormatter(propertyMap)).thenReturn(dataFormatter);
        when(dataFormatterFactory.getPropertySpec("name")).thenReturn(propertySpec);
        when(strategy.isExportContinuousData()).thenReturn(false);
        doReturn(Arrays.asList(reading1)).when(meter1).getReadings(exportPeriod, readingType1);
        doReturn(Arrays.asList(reading2)).when(meter2).getReadings(exportPeriod, readingType1);
        when(dataFormatter.processData(any())).thenReturn(formattedData);
        doAnswer(invocation -> {
            List<ExportData> exportData = ((Stream<ExportData>) invocation.getArguments()[0]).collect(Collectors.toList());
            passedStreams.add(exportData);
            return formattedData;
        }).when(dataFormatter).processData(any());
        when(reading1.getSource()).thenReturn("reading1");
        when(reading2.getSource()).thenReturn("reading2");
        MeterReadingData newItemData = new MeterReadingData(this.newItem, MeterReadingImpl.of(ReadingImpl.reading(reading1, readingType1)), DefaultStructureMarker.createRoot(clock, "newItem"));
        MeterReadingData existItemData = new MeterReadingData(this.existingItem, MeterReadingImpl.of(ReadingImpl.reading(reading2, readingType1)), DefaultStructureMarker.createRoot(clock, "newItem"));
        when(readingTypeDataSelector.selectData(dataExportOccurrence)).thenReturn(Arrays.<ExportData>asList(newItemData, existItemData).stream());
    }

    @After
    public void tearDown() {
        passedStreams.clear();
    }

    @Test
    public void testDataFormatterGetsTheRightNotifications() {
        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        ArgumentCaptor<Logger> logCaptor = ArgumentCaptor.forClass(Logger.class);
        ArgumentCaptor<Stream> readingCaptor1 = ArgumentCaptor.forClass(Stream.class);
        ArgumentCaptor<Stream> readingCaptor2 = ArgumentCaptor.forClass(Stream.class);

        InOrder inOrder = inOrder(dataFormatter);
        inOrder.verify(dataFormatter).startExport(eq(dataExportOccurrence), logCaptor.capture());
        inOrder.verify(dataFormatter).startItem(newItem);
        inOrder.verify(dataFormatter).processData(readingCaptor1.capture());
        inOrder.verify(dataFormatter).endItem(newItem);
        inOrder.verify(dataFormatter).startItem(existingItem);
        inOrder.verify(dataFormatter).processData(readingCaptor2.capture());
        inOrder.verify(dataFormatter).endItem(existingItem);
        inOrder.verify(dataFormatter).endExport();

        logCaptor.getValue().log(Level.WARNING, "testHandler");
        assertThat(logRecorder.getRecords()).hasSize(3);
        LogRecord logRecord = logRecorder.getRecords().get(2);
        assertThat(logRecord.getLevel()).isEqualTo(Level.WARNING);
        assertThat(logRecord.getMessage()).isEqualTo("testHandler");

        List<MeterReadingData> readingList1 = passedStreams.get(0).stream().map(MeterReadingData.class::cast).collect(Collectors.toList());
        assertThat(readingList1).hasSize(1);
        assertThat(readingList1.get(0).getMeterReading().getReadings()).has(new ReadingFor(reading1));
        List<MeterReadingData> readingList2 = passedStreams.get(1).stream().map(MeterReadingData.class::cast).collect(Collectors.toList());
        assertThat(readingList2).hasSize(1);
        assertThat(readingList2.get(0).getMeterReading().getReadings()).has(new ReadingFor(reading2));

        verify(destination).send(any());
    }

    @Test
    public void testDataFormatterGetsTheRightNotificationsForIntervalReadings() {
        when(readingType1.isRegular()).thenReturn(true);
        MeterReadingImpl meterReading1 = getMeterReadingWithIntervalBlock(newItem, Collections.singletonList(reading1));
        MeterReadingData newItemData = new MeterReadingData(this.newItem, meterReading1, DefaultStructureMarker.createRoot(clock, "newItem"));
        MeterReadingImpl meterReading2 = getMeterReadingWithIntervalBlock(existingItem, Collections.singletonList(reading2));
        MeterReadingData existItemData = new MeterReadingData(this.existingItem, meterReading2, DefaultStructureMarker.createRoot(clock, "newItem"));
        when(readingTypeDataSelector.selectData(dataExportOccurrence)).thenReturn(Arrays.<ExportData>asList(newItemData, existItemData).stream());

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        ArgumentCaptor<Logger> logCaptor = ArgumentCaptor.forClass(Logger.class);
        ArgumentCaptor<Stream> readingCaptor1 = ArgumentCaptor.forClass(Stream.class);
        ArgumentCaptor<Stream> readingCaptor2 = ArgumentCaptor.forClass(Stream.class);

        InOrder inOrder = inOrder(dataFormatter);
        inOrder.verify(dataFormatter).startExport(eq(dataExportOccurrence), logCaptor.capture());
        inOrder.verify(dataFormatter).startItem(newItem);
        inOrder.verify(dataFormatter).processData(readingCaptor1.capture());
        inOrder.verify(dataFormatter).endItem(newItem);
        inOrder.verify(dataFormatter).startItem(existingItem);
        inOrder.verify(dataFormatter).processData(readingCaptor2.capture());
        inOrder.verify(dataFormatter).endItem(existingItem);
        inOrder.verify(dataFormatter).endExport();

        logCaptor.getValue().log(Level.WARNING, "testHandler");
        assertThat(logRecorder.getRecords()).hasSize(3);
        LogRecord logRecord = logRecorder.getRecords().get(2);
        assertThat(logRecord.getLevel()).isEqualTo(Level.WARNING);
        assertThat(logRecord.getMessage()).isEqualTo("testHandler");

        List<MeterReadingData> readingList1 = passedStreams.get(0).stream().map(MeterReadingData.class::cast).collect(Collectors.toList());
        assertThat(readingList1).hasSize(1);
        assertThat(readingList1.get(0).getMeterReading().getIntervalBlocks()).hasSize(1);
        assertThat(readingList1.get(0).getMeterReading().getIntervalBlocks().get(0).getIntervals()).has(new IntervalReadingFor(reading1));
        List<MeterReadingData> readingList2 = passedStreams.get(1).stream().map(MeterReadingData.class::cast).collect(Collectors.toList());
        assertThat(readingList2).hasSize(1);
        assertThat(readingList2.get(0).getMeterReading().getIntervalBlocks()).hasSize(1);
        assertThat(readingList2.get(0).getMeterReading().getIntervalBlocks().get(0).getIntervals()).has(new IntervalReadingFor(reading2));

        verify(destination).send(any());
    }

    @Test
    public void testDataFormatterGetsTheRightNotificationsInTheRightTransactions() {
        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        verify(dataFormatter, transactionService.notInTransaction()).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter, transactionService.inTransaction(2)).startItem(newItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading1"));
        verify(dataFormatter, transactionService.inTransaction(2)).endItem(newItem);
        verify(dataFormatter, transactionService.inTransaction(4)).startItem(existingItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading2"));
        verify(dataFormatter, transactionService.inTransaction(4)).endItem(existingItem);
        verify(dataFormatter, transactionService.notInTransaction()).endExport();

        verify(newItem, transactionService.inTransaction(6)).update();
        verify(existingItem, transactionService.inTransaction(6)).update();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasCommitted();
        transactionService.assertThatTransaction(5).wasCommitted();

        verify(destination).send(any());
    }

    @Test
    public void testStartExportThrowsFatalException() {
        doThrow(new FatalDataExportException(new RuntimeException())).when(dataFormatter).startExport(eq(dataExportOccurrence), any());

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter, never()).startItem(newItem);
        verify(dataFormatter, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().contains(reading1))));
        verify(dataFormatter, never()).endItem(newItem);
        verify(dataFormatter, never()).startItem(existingItem);
        verify(dataFormatter, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().contains(reading2))));
        verify(dataFormatter, never()).endItem(existingItem);
        verify(dataFormatter, never()).endExport();

        verify(destination, never()).send(any());

    }

    @Test
    public void testStartExportThrowsRuntimeException() {
        doThrow(new RuntimeException()).when(dataFormatter).startExport(eq(dataExportOccurrence), any());

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter, never()).startItem(newItem);
        verify(dataFormatter, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().contains(reading1))));
        verify(dataFormatter, never()).endItem(newItem);
        verify(dataFormatter, never()).startItem(existingItem);
        verify(dataFormatter, never()).processData(argThat(matches(r -> ((MeterReadingData) r).getMeterReading().getReadings().contains(reading2))));
        verify(dataFormatter, never()).endItem(existingItem);
        verify(dataFormatter, never()).endExport();

        verify(destination, never()).send(any());
    }

    @Test
    public void testStartItemThrowsFatalException() {
        doThrow(new FatalDataExportException(new RuntimeException())).when(dataFormatter).startItem(existingItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            e.printStackTrace();
            // expected
        }

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter).startItem(newItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading1"));
        verify(dataFormatter).endItem(newItem);
        verify(dataFormatter).startItem(existingItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading2").negate());
        verify(dataFormatter, never()).endItem(existingItem);
        verify(dataFormatter, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted(); // newItem
        transactionService.assertThatTransaction(3).wasCommitted(); // log success of newItem
        transactionService.assertThatTransaction(4).wasNotCommitted(); // existingItem
        transactionService.assertThatTransaction(5).wasCommitted(); // log failure of existingItem

        verify(destination, never()).send(any());
    }

    Predicate<List<? extends List<ExportData>>> hasStreamContainingReadingFor(String source) {
        return list -> list.stream().anyMatch(stream -> stream.stream().anyMatch(exportData -> {
            return ((MeterReadingData) exportData).getMeterReading().getReadings().stream().anyMatch(rd -> {
                return rd.getSource().equals(source);
            });
        }));
    }

    @Test
    public void testStartItemThrowsRuntimeException() {
        doThrow(new RuntimeException()).when(dataFormatter).startItem(existingItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter).startItem(newItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading1"));
        verify(dataFormatter).endItem(newItem);
        verify(dataFormatter).startItem(existingItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading2").negate());
        verify(dataFormatter, never()).endItem(existingItem);
        verify(dataFormatter, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasNotCommitted();

        verify(destination, never()).send(any());
    }

    @Test
    public void testStartItemThrowsDataExportException() {
        doThrow(DataExportException.class).when(dataFormatter).startItem(newItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter).startItem(newItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading1").negate());
        verify(dataFormatter, never()).endItem(newItem);
        verify(dataFormatter).startItem(existingItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading2"));
        verify(dataFormatter).endItem(existingItem);
        verify(dataFormatter).endExport();

        transactionService.assertThatTransaction(2).wasNotCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();

        verify(destination).send(any());
    }

    @Test
    public void testProcessItemThrowsFatalException() {
        doAnswer(invocation -> {
            List<ExportData> exportData = ((Stream<ExportData>) invocation.getArguments()[0]).collect(Collectors.toList());
            passedStreams.add(exportData);
            if (passedStreams.size() == 2) {
                throw new FatalDataExportException(new RuntimeException());
            }
            return formattedData;
        }).when(dataFormatter).processData(any());

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter).startItem(newItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading1"));
        verify(dataFormatter).endItem(newItem);
        verify(dataFormatter).startItem(existingItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading2"));
        verify(dataFormatter, never()).endItem(existingItem);
        verify(dataFormatter, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasNotCommitted();

        verify(destination, never()).send(any());
    }

    @Test
    public void testProcessItemThrowsRuntimeException() {
        doAnswer(invocation -> {
            List<ExportData> exportData = ((Stream<ExportData>) invocation.getArguments()[0]).collect(Collectors.toList());
            passedStreams.add(exportData);
            if (passedStreams.size() == 2) {
                throw new RuntimeException();
            }
            return formattedData;
        }).when(dataFormatter).processData(any());

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter).startItem(newItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading1"));
        verify(dataFormatter).endItem(newItem);
        verify(dataFormatter).startItem(existingItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading2"));
        verify(dataFormatter, never()).endItem(existingItem);
        verify(dataFormatter, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasNotCommitted();
        transactionService.assertThatTransaction(5).wasCommitted();

        verify(destination, never()).send(any());
    }

    @Test
    public void testProcessItemThrowsDataExportException() {
        doAnswer(invocation -> {
            List<ExportData> exportData = ((Stream<ExportData>) invocation.getArguments()[0]).collect(Collectors.toList());
            passedStreams.add(exportData);
            if (passedStreams.size() == 1) {
                throw mock(DataExportException.class);
            }
            return formattedData;
        }).when(dataFormatter).processData(any());

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter).startItem(newItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading1"));
        verify(dataFormatter, never()).endItem(newItem);
        verify(dataFormatter).startItem(existingItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading2"));
        verify(dataFormatter).endItem(existingItem);
        verify(dataFormatter).endExport();

        transactionService.assertThatTransaction(2).wasNotCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();

        verify(destination).send(any());
    }

    @Test
    public void testEndItemThrowsFatalException() {
        doThrow(new FatalDataExportException(new RuntimeException())).when(dataFormatter).endItem(existingItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter).startItem(newItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading1"));
        verify(dataFormatter).endItem(newItem);
        verify(dataFormatter).startItem(existingItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading2"));
        verify(dataFormatter).endItem(existingItem);
        verify(dataFormatter, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasNotCommitted();
        transactionService.assertThatTransaction(5).wasCommitted();

        verify(destination, never()).send(any());
    }

    @Test
    public void testEndItemThrowsRuntimeException() {
        doThrow(new RuntimeException()).when(dataFormatter).endItem(existingItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try {
            try (TransactionContext context = transactionService.getContext()) {
                executor.execute(occurrence);
            }
            executor.postExecute(occurrence);
            fail("expected FatalDataExportException");
        } catch (FatalDataExportException e) {
            // expected
        }

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter).startItem(newItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading1"));
        verify(dataFormatter).endItem(newItem);
        verify(dataFormatter).startItem(existingItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading2"));
        verify(dataFormatter).endItem(existingItem);
        verify(dataFormatter, never()).endExport();

        transactionService.assertThatTransaction(2).wasCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();
        transactionService.assertThatTransaction(4).wasNotCommitted();
        transactionService.assertThatTransaction(5).wasCommitted();

        verify(destination, never()).send(any());
    }

    @Test
    public void testEndItemThrowsDataExportException() {
        doThrow(DataExportException.class).when(dataFormatter).endItem(newItem);

        DataExportTaskExecutor executor = new DataExportTaskExecutor(dataExportService, transactionService, new LocalFileWriter(dataExportService), thesaurus);

        try (TransactionContext context = transactionService.getContext()) {
            executor.execute(occurrence);
        }
        executor.postExecute(occurrence);

        verify(dataFormatter).startExport(eq(dataExportOccurrence), any());
        verify(dataFormatter).startItem(newItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading1"));
        verify(dataFormatter).endItem(newItem);
        verify(dataFormatter).startItem(existingItem);
        assertThat(passedStreams).matches(hasStreamContainingReadingFor("reading2"));
        verify(dataFormatter).endItem(existingItem);
        verify(dataFormatter).endExport();

        transactionService.assertThatTransaction(2).wasNotCommitted();
        transactionService.assertThatTransaction(3).wasCommitted();

        verify(destination).send(any());
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

    private MeterReadingImpl getMeterReadingWithIntervalBlock(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(buildIntervalBlock(item, readings));
        return meterReading;
    }

    private IntervalBlockImpl buildIntervalBlock(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        return readings.stream()
                .map(IntervalReadingRecord.class::cast)
                .collect(
                        () -> IntervalBlockImpl.of(item.getReadingType().getMRID()),
                        (block, reading) -> block.addIntervalReading(forReadingType(reading, item.getReadingType())),
                        (b1, b2) -> b1.addAllIntervalReadings(b2.getIntervals())
                );
    }

    private IntervalReading forReadingType(IntervalReadingRecord readingRecord, ReadingType readingType) {
        return intervalReading(readingRecord, readingType);
    }


}