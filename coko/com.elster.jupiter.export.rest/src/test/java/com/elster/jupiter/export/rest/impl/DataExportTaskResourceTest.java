/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ExportTaskFinder;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.time.Never;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static com.elster.jupiter.export.rest.impl.DataExportTaskResource.X_CONNEXO_APPLICATION_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataExportTaskResourceTest extends DataExportApplicationJerseyTest {

    private static final ZonedDateTime NEXT_EXECUTION = ZonedDateTime.of(2015, 1, 13, 0, 0, 0, 0, ZoneId.systemDefault());
    private static final long TASK_ID = 750L;
    private static final String TASK_NAME = "Name";
    private static final long OK_VERSION = 41L;
    private static final long BAD_VERSION = 35L;

    @Mock
    protected ExportTask exportTask;
    @Mock
    private MeterReadingSelectorConfig selectorConfig;
    @Mock
    private DataSelectorFactory dataSelectorFactory;
    @Mock
    private DataFormatterFactory dataFormatterFactory;
    @Mock
    private DataExportOccurrenceFinder dataExportOccurrenceFinder;
    @Mock
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private RelativePeriod exportPeriod;
    @Mock
    private RelativePeriod updatePeriod, updateWindow;
    @Mock
    private DataExportStrategy strategy;
    @Mock
    private QueryExecutor<DataExportOccurrence> queryExecutor;
    @Mock
    private FileDestination newDestination;
    @Mock
    private ExportTaskFinder exportTaskFinder;
    @Mock
    private DataExportOccurrence occurrence;
    @Mock
    private LogEntryFinder logEntryFinder;
    @Mock
    private QueryStream queryStream;

    private DataExportTaskBuilder builder;

    @Before
    public void setUpMocks() {
        builder = FakeBuilder.initBuilderStub(exportTask,
                DataExportTaskBuilder.class,
                DataExportTaskBuilder.PropertyBuilder.class,
                DataExportTaskBuilder.CustomSelectorBuilder.class,
                DataExportTaskBuilder.EventSelectorBuilder.class,
                DataExportTaskBuilder.MeterReadingSelectorBuilder.class,
                DataExportTaskBuilder.UsagePointReadingSelectorBuilder.class
        );
        when(transactionService.execute(any())).thenAnswer(invocation -> ((Transaction<?>) invocation.getArguments()[0]).perform());
        when(dataExportService.findExportTasks()).thenReturn(exportTaskFinder);
        when(exportTask.getStandardDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));
        when(selectorConfig.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        when(selectorConfig.getExportPeriod()).thenReturn(exportPeriod);
        when(exportPeriod.getRelativeDateFrom()).thenReturn(new RelativeDate(RelativeField.DAY.minus(1)));
        when(exportPeriod.getRelativeDateTo()).thenReturn(new RelativeDate());
        when(selectorConfig.getStrategy()).thenReturn(strategy);
        when(strategy.getUpdateWindow()).thenReturn(Optional.empty());
        when(strategy.getUpdatePeriod()).thenReturn(Optional.of(exportPeriod));
        when(exportTask.getNextExecution()).thenReturn(NEXT_EXECUTION.toInstant());
        when(meteringGroupsService.findEndDeviceGroup(5)).thenReturn(Optional.of(endDeviceGroup));
        when(meteringGroupsService.findUsagePointGroup(5)).thenReturn(Optional.of(usagePointGroup));
        when(exportTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(dataExportService.newBuilder()).thenReturn(builder);
        when(exportTask.getOccurrencesFinder()).thenReturn(dataExportOccurrenceFinder);
        when(exportTask.getId()).thenReturn(TASK_ID);
        when(exportTask.getName()).thenReturn(TASK_NAME);
        when(exportTask.getLastOccurrence()).thenReturn(Optional.empty());
        when(exportTask.getLastRun()).thenReturn(Optional.empty());
        when(exportTask.getVersion()).thenReturn(OK_VERSION);

        when(this.dataSelectorFactory.getName()).thenReturn("DataSelectorFactor");
        when(this.dataSelectorFactory.getDisplayName()).thenReturn("DataSelectorFactor");
        when(dataExportService.getDataSelectorFactory(anyString())).thenReturn(Optional.of(this.dataSelectorFactory));
        when(exportTask.getDataSelectorFactory()).thenReturn(this.dataSelectorFactory);
        when(exportTask.getDataFormatterFactory()).thenReturn(this.dataFormatterFactory);
        when(exportTask.getApplication()).thenReturn("MultiSense");
        doReturn(Optional.of(exportTask)).when(dataExportService).findExportTask(TASK_ID);
        doReturn(Optional.of(exportTask)).when(dataExportService).findAndLockExportTask(TASK_ID, OK_VERSION);
        doReturn(Optional.empty()).when(dataExportService).findAndLockExportTask(TASK_ID, BAD_VERSION);
    }

    @Test
    public void getApplicationSpecificTasks() {
        when(exportTaskFinder.ofApplication("MultiSense")).thenReturn(exportTaskFinder);
        doReturn(Stream.of(exportTask)).when(exportTaskFinder).stream();

        // Business method
        String response = target("/dataexporttask").request().header(X_CONNEXO_APPLICATION_NAME, "MDC").get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.dataExportTasks")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.dataExportTasks[0].id")).isEqualTo(Long.valueOf(TASK_ID).intValue());
        assertThat(jsonModel.<String>get("$.dataExportTasks[0].name")).isEqualTo(TASK_NAME);
        assertThat(jsonModel.<Number>get("$.dataExportTasks[0].version")).isEqualTo(Long.valueOf(OK_VERSION).intValue());
        assertThat(jsonModel.<Number>get("$.dataExportTasks[0].nextRun")).isEqualTo(NEXT_EXECUTION.toInstant().toEpochMilli());
    }

    @Test
    public void getTask() {
        // Business method
        String response = target("/dataexporttask/" + TASK_ID).request().header(X_CONNEXO_APPLICATION_NAME, "MDC").get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(Long.valueOf(TASK_ID).intValue());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo(TASK_NAME);
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(Long.valueOf(OK_VERSION).intValue());
        assertThat(jsonModel.<Number>get("$.nextRun")).isEqualTo(NEXT_EXECUTION.toInstant().toEpochMilli());
    }

    @Test
    public void getNonexistentTask() {
        long id = 123456;
        when(dataExportService.findExportTask(id)).thenReturn(Optional.empty());

        // Business method
        Response response = target("/dataexporttask/" + id).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void triggerTask() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.version = OK_VERSION;

        // Business method
        Response response = target("/dataexporttask/" + TASK_ID + "/trigger").request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(exportTask).triggerNow();
    }

    @Test
    public void createTask() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.name = "newName";
        info.nextRun = Instant.ofEpochMilli(250L);
        info.standardDataSelector = new StandardDataSelectorInfo();
        info.standardDataSelector.deviceGroup = new IdWithNameInfo();
        info.standardDataSelector.deviceGroup.id = 5;
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        info.dataSelector = new SelectorInfo();
        info.dataSelector.selectorType = SelectorType.DEFAULT_READINGS;
        info.dataSelector.name = "Device readings data selector";
        DestinationInfo fileDestinationInfo = new DestinationInfo();
        fileDestinationInfo.type = DestinationType.FILE;
        fileDestinationInfo.fileLocation = "";
        fileDestinationInfo.fileName = "file";
        fileDestinationInfo.fileExtension = "txt";
        info.destinations.add(fileDestinationInfo);

        DestinationInfo emailDestinationInfo = new DestinationInfo();
        emailDestinationInfo.type = DestinationType.EMAIL;
        emailDestinationInfo.fileName = "attachment";
        emailDestinationInfo.fileExtension = "csv";
        emailDestinationInfo.recipients = "user1@elster.com;user2@elster.com";
        emailDestinationInfo.subject = "daily report";
        info.destinations.add(emailDestinationInfo);

        DestinationInfo ftpDestinationInfo = new DestinationInfo();
        ftpDestinationInfo.type = DestinationType.FTP;
        ftpDestinationInfo.fileLocation = "";
        ftpDestinationInfo.fileName = "ftpfile";
        ftpDestinationInfo.fileExtension = "ftptxt";
        ftpDestinationInfo.server = "ftpserver";
        ftpDestinationInfo.password = "ftppassword";
        ftpDestinationInfo.user = "ftpuser";
        ftpDestinationInfo.port = 21;
        info.destinations.add(ftpDestinationInfo);

        DestinationInfo ftpsDestinationInfo = new DestinationInfo();
        ftpsDestinationInfo.type = DestinationType.FTPS;
        ftpsDestinationInfo.fileLocation = "";
        ftpsDestinationInfo.fileName = "ftpsfile";
        ftpsDestinationInfo.fileExtension = "ftpstxt";
        ftpsDestinationInfo.server = "ftpsserver";
        ftpsDestinationInfo.password = "ftpspassword";
        ftpsDestinationInfo.user = "ftpsuser";
        ftpsDestinationInfo.port = 20;
        info.destinations.add(ftpsDestinationInfo);

        Entity<DataExportTaskInfo> json = Entity.json(info);

        // Business method
        Response response = target("/dataexporttask").request().header(X_CONNEXO_APPLICATION_NAME, "MDC").post(json);

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        ArgumentCaptor<String> applicationNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(builder).setApplication(applicationNameCaptor.capture());
        assertThat(applicationNameCaptor.getValue()).isEqualTo("MultiSense");

        verify(builder).selectingMeterReadings();

        verify(exportTask).addFileDestination("", "file", "txt");
        verify(exportTask).addEmailDestination("user1@elster.com;user2@elster.com", "daily report", "attachment", "csv");
        verify(exportTask).addFtpDestination("ftpserver", 21, "ftpuser", "ftppassword", "", "ftpfile", "ftptxt");
        verify(exportTask).addFtpsDestination("ftpsserver", 20, "ftpsuser", "ftpspassword", "", "ftpsfile", "ftpstxt");
    }

    @Test
    public void updateTask() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.standardDataSelector = new StandardDataSelectorInfo();
        info.standardDataSelector.deviceGroup = new IdWithNameInfo();
        info.standardDataSelector.deviceGroup.id = 5;
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        info.dataSelector = new SelectorInfo();
        info.dataSelector.name = "Standard Data Selector";
        info.version = OK_VERSION;
        DestinationInfo fileDestinationInfo = new DestinationInfo();
        fileDestinationInfo.id = 0; // new
        fileDestinationInfo.type = DestinationType.FILE;
        fileDestinationInfo.fileLocation = "";
        fileDestinationInfo.fileName = "file";
        fileDestinationInfo.fileExtension = "txt";
        info.destinations.add(fileDestinationInfo);

        Entity<DataExportTaskInfo> json = Entity.json(info);

        // Business method
        Response response = target("/dataexporttask/" + TASK_ID).request().put(json);

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void updateTaskDestination() {
        EmailDestination obsolete = mock(EmailDestination.class);
        EmailDestination toUpdate = mock(EmailDestination.class);
        when(exportTask.getDestinations()).thenReturn(Arrays.asList(obsolete, toUpdate));
        when(obsolete.getId()).thenReturn(7772L);
        when(toUpdate.getId()).thenReturn(7773L);
        when(exportTask.addFileDestination("", "file", "txt")).thenReturn(newDestination);

        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.name = "newName";
        info.nextRun = Instant.ofEpochMilli(250L);
        info.dataSelector = new SelectorInfo();
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        info.version = OK_VERSION;
        DestinationInfo fileDestinationInfo = new DestinationInfo();
        fileDestinationInfo.id = 0; // new
        fileDestinationInfo.type = DestinationType.FILE;
        fileDestinationInfo.fileLocation = "";
        fileDestinationInfo.fileName = "file";
        fileDestinationInfo.fileExtension = "txt";
        info.destinations.add(fileDestinationInfo);
        DestinationInfo emailDestinationInfo = new DestinationInfo();
        emailDestinationInfo.id = 7773L;
        emailDestinationInfo.type = DestinationType.EMAIL;
        emailDestinationInfo.fileName = "attachment";
        emailDestinationInfo.fileExtension = "csv";
        emailDestinationInfo.recipients = "user1@elster.com;user2@elster.com";
        emailDestinationInfo.subject = "daily report";
        info.destinations.add(emailDestinationInfo);

        Entity<DataExportTaskInfo> json = Entity.json(info);

        // Business method
        Response response = target("/dataexporttask/" + TASK_ID).request().put(json);

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(exportTask).removeDestination(obsolete);
        verify(exportTask, never()).removeDestination(newDestination);
        verify(toUpdate).setRecipients("user1@elster.com;user2@elster.com");
        verify(toUpdate).setSubject("daily report");
        verify(toUpdate).setAttachmentName("attachment");
        verify(toUpdate).setAttachmentExtension("csv");
        verify(toUpdate).save();
        verify(exportTask).addFileDestination("", "file", "txt");
    }

    @Test
    public void updateTaskBadVersion() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.name = "newName";
        info.nextRun = Instant.ofEpochMilli(250L);
        info.dataSelector = new SelectorInfo();
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        info.version = BAD_VERSION;

        Entity<DataExportTaskInfo> json = Entity.json(info);

        // Business method
        Response response = target("/dataexporttask/" + TASK_ID).request().put(json);

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void deleteTaskBadVersion() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.name = "newName";
        info.version = BAD_VERSION;
        Entity<DataExportTaskInfo> json = Entity.json(info);

        // Business method
        Response response = target("/dataexporttask/" + TASK_ID).request().build(HttpMethod.DELETE, json).invoke();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void triggerNowBadVersion() {
        ExportTask exportTask = mock(ExportTask.class);
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.name = "newName";
        info.version = BAD_VERSION;
        Entity<DataExportTaskInfo> json = Entity.json(info);

        // Business method
        Response response = target("/dataexporttask/" + TASK_ID + "/trigger").request().put(json);

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(exportTask, never()).triggerNow();
    }

    @Test
    public void getMeterDataSources() {
        Instant lastRun = Instant.now();
        DataExportOccurrence dataExportOccurrence = mock(DataExportOccurrence.class);
        when(dataExportOccurrence.getId()).thenReturn(13L);
        List<ReadingTypeDataExportItem> items = Arrays.asList(
                mockExportItem(dataExportOccurrence, mockMeter("SPE001", "001"), lastRun),
                mockExportItem(dataExportOccurrence, mockMeter("SPE002", "002"), lastRun),
                mockExportItem(dataExportOccurrence, mockMeter("SPE003", "003"), lastRun)
        );
        MeterReadingSelectorConfig selectorConfig = mock(MeterReadingSelectorConfig.class);
        when(exportTask.getStandardDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));
        doAnswer(invocationOnMock -> {
            DataSelectorConfig.DataSelectorConfigVisitor visitor = (DataSelectorConfig.DataSelectorConfigVisitor) invocationOnMock.getArguments()[0];
            visitor.visit(selectorConfig);
            return null;
        }).when(selectorConfig).apply(any());
        doReturn(items).when(selectorConfig).getExportItems();

        // Business method
        String response = target("/dataexporttask/" + TASK_ID + "/datasources")
                .queryParam("start", 1).queryParam("limit", 1).request()
                .header(X_CONNEXO_APPLICATION_NAME, "MDC")
                .get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<?>>get("$.dataSources")).hasSize(1);
        assertThat(jsonModel.<String>get("$.dataSources[0].name")).isEqualTo("SPE002");
        assertThat(jsonModel.<String>get("$.dataSources[0].serialNumber")).isEqualTo("002");
        assertThat(jsonModel.<String>get("$.dataSources[0].readingType.mRID")).isEqualTo("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        assertThat(jsonModel.<Number>get("$.dataSources[0].occurrenceId")).isEqualTo(13);
    }

    @Test
    public void getUsagePointDataSources() {
        Instant lastRun = Instant.now();
        DataExportOccurrence dataExportOccurrence = mock(DataExportOccurrence.class);
        when(dataExportOccurrence.getId()).thenReturn(13L);
        List<ReadingTypeDataExportItem> items = Arrays.asList(
                mockExportItem(dataExportOccurrence, mockUsagePoint("UP001", "Under construction"), lastRun),
                mockExportItem(dataExportOccurrence, mockUsagePoint("UP002", "Connected"), lastRun),
                mockExportItem(dataExportOccurrence, mockUsagePoint("UP003", "Logically disconnected"), lastRun)
        );
        UsagePointReadingSelectorConfig selectorConfig = mock(UsagePointReadingSelectorConfig.class);
        when(exportTask.getStandardDataSelectorConfig()).thenReturn(Optional.of(selectorConfig));
        doAnswer(invocationOnMock -> {
            DataSelectorConfig.DataSelectorConfigVisitor visitor = (DataSelectorConfig.DataSelectorConfigVisitor) invocationOnMock.getArguments()[0];
            visitor.visit(selectorConfig);
            return null;
        }).when(selectorConfig).apply(any());
        doReturn(items).when(selectorConfig).getExportItems();

        // Business method
        String response = target("/dataexporttask/" + TASK_ID + "/datasources")
                .queryParam("start", 1).queryParam("limit", 1).request()
                .header(X_CONNEXO_APPLICATION_NAME, "MDC")
                .get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<?>>get("$.dataSources")).hasSize(1);
        assertThat(jsonModel.<String>get("$.dataSources[0].name")).isEqualTo("UP002");
        assertThat(jsonModel.<String>get("$.dataSources[0].connectionState")).isEqualTo("Connected");
        assertThat(jsonModel.<String>get("$.dataSources[0].readingType.mRID")).isEqualTo("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        assertThat(jsonModel.<Number>get("$.dataSources[0].occurrenceId")).isEqualTo(13);
    }

    @Test
    public void testCreateTaskWithStandardMeterReadingsSelector() {
        long exportPeriodId = 13L, updatePeriodId = 14L, updateWindowId = 15L;
        when(timeService.findRelativePeriod(exportPeriodId)).thenReturn(Optional.of(exportPeriod));
        when(timeService.findRelativePeriod(updatePeriodId)).thenReturn(Optional.of(updatePeriod));
        when(timeService.findRelativePeriod(updateWindowId)).thenReturn(Optional.of(updateWindow));

        DataExportTaskInfo info = new DataExportTaskInfo();
        info.name = "newName";
        // data selector
        info.dataSelector = new SelectorInfo();
        info.dataSelector.selectorType = SelectorType.DEFAULT_READINGS;
        info.standardDataSelector = new StandardDataSelectorInfo();
        info.standardDataSelector.deviceGroup = new IdWithNameInfo();
        info.standardDataSelector.deviceGroup.id = 5;
        info.standardDataSelector.exportComplete = true;
        info.standardDataSelector.exportContinuousData = true;
        info.standardDataSelector.exportUpdate = true;
        info.standardDataSelector.exportAdjacentData = true;
        info.standardDataSelector.exportPeriod = new RelativePeriodInfo();
        info.standardDataSelector.exportPeriod.id = exportPeriodId;
        info.standardDataSelector.updateWindow = new RelativePeriodInfo();
        info.standardDataSelector.updateWindow.id = updateWindowId;
        info.standardDataSelector.updatePeriod = new RelativePeriodInfo();
        info.standardDataSelector.updatePeriod.id = updatePeriodId;
        info.standardDataSelector.validatedDataOption = ValidatedDataOption.EXCLUDE_INTERVAL;
        // data processor
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        // destination
        DestinationInfo fileDestinationInfo = new DestinationInfo();
        fileDestinationInfo.type = DestinationType.FILE;
        fileDestinationInfo.fileLocation = "";
        fileDestinationInfo.fileName = "file";
        fileDestinationInfo.fileExtension = "txt";
        info.destinations.add(fileDestinationInfo);

        // Business method
        Response response = target("/dataexporttask").request().header(X_CONNEXO_APPLICATION_NAME, "MDC").post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(builder).selectingMeterReadings();
        DataExportTaskBuilder.MeterReadingSelectorBuilder selectorBuilder = (DataExportTaskBuilder.MeterReadingSelectorBuilder) this.builder;
        verify(selectorBuilder).fromExportPeriod(exportPeriod);
        verify(selectorBuilder).fromUpdatePeriod(updatePeriod);
        verify(selectorBuilder).withUpdateWindow(updateWindow);
        verify(selectorBuilder).withValidatedDataOption(ValidatedDataOption.EXCLUDE_INTERVAL);
        verify(selectorBuilder).fromEndDeviceGroup(endDeviceGroup);
        verify(selectorBuilder).continuousData(true);
        verify(selectorBuilder).exportComplete(true);
        verify(selectorBuilder).exportUpdate(true);
    }

    @Test
    public void testCreateTaskWithStandardUsagePointReadingsSelector() {
        long exportPeriodId = 13L;
        when(timeService.findRelativePeriod(exportPeriodId)).thenReturn(Optional.of(exportPeriod));
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.name = "newName";
        // data selector
        info.dataSelector = new SelectorInfo();
        info.dataSelector.selectorType = SelectorType.DEFAULT_USAGE_POINT_READINGS;
        info.standardDataSelector = new StandardDataSelectorInfo();
        info.standardDataSelector.usagePointGroup = new IdWithNameInfo();
        info.standardDataSelector.usagePointGroup.id = 5;
        info.standardDataSelector.exportComplete = true;
        info.standardDataSelector.exportContinuousData = true;
        info.standardDataSelector.exportPeriod = new RelativePeriodInfo();
        info.standardDataSelector.exportPeriod.id = exportPeriodId;
        info.standardDataSelector.validatedDataOption = ValidatedDataOption.EXCLUDE_INTERVAL;
        // data processor
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        // destination
        DestinationInfo fileDestinationInfo = new DestinationInfo();
        fileDestinationInfo.type = DestinationType.FILE;
        fileDestinationInfo.fileLocation = "";
        fileDestinationInfo.fileName = "file";
        fileDestinationInfo.fileExtension = "txt";
        info.destinations.add(fileDestinationInfo);

        // Business method
        Response response = target("/dataexporttask").request().header(X_CONNEXO_APPLICATION_NAME, "MDC").post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(builder).selectingUsagePointReadings();
        DataExportTaskBuilder.UsagePointReadingSelectorBuilder selectorBuilder = (DataExportTaskBuilder.UsagePointReadingSelectorBuilder) this.builder;
        verify(selectorBuilder).fromExportPeriod(exportPeriod);
        verify(selectorBuilder).withValidatedDataOption(ValidatedDataOption.EXCLUDE_INTERVAL);
        verify(selectorBuilder).fromUsagePointGroup(usagePointGroup);
        verify(selectorBuilder).continuousData(true);
        verify(selectorBuilder).exportComplete(true);
    }

    @Test
    public void testCreateTaskWithStandardEventSelector() {
        long exportPeriodId = 13L;
        when(timeService.findRelativePeriod(exportPeriodId)).thenReturn(Optional.of(exportPeriod));

        DataExportTaskInfo info = new DataExportTaskInfo();
        info.name = "newName";
        // data selector
        info.dataSelector = new SelectorInfo();
        info.dataSelector.selectorType = SelectorType.DEFAULT_EVENTS;
        info.standardDataSelector = new StandardDataSelectorInfo();
        info.standardDataSelector.deviceGroup = new IdWithNameInfo();
        info.standardDataSelector.deviceGroup.id = 5;
        info.standardDataSelector.exportPeriod = new RelativePeriodInfo();
        info.standardDataSelector.exportPeriod.id = exportPeriodId;
        // data processor
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        // destination
        DestinationInfo fileDestinationInfo = new DestinationInfo();
        fileDestinationInfo.type = DestinationType.FILE;
        fileDestinationInfo.fileLocation = "";
        fileDestinationInfo.fileName = "file";
        fileDestinationInfo.fileExtension = "txt";
        info.destinations.add(fileDestinationInfo);

        // Business method
        Response response = target("/dataexporttask").request().header(X_CONNEXO_APPLICATION_NAME, "MDC").post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(builder).selectingEventTypes();
        DataExportTaskBuilder.EventSelectorBuilder selectorBuilder = (DataExportTaskBuilder.EventSelectorBuilder) this.builder;
        verify(selectorBuilder).fromExportPeriod(exportPeriod);
        verify(selectorBuilder).fromEndDeviceGroup(endDeviceGroup);
    }

    @Test
    public void testGetAllDataExportTaskHistory() throws Exception {
        DataExportTaskInfo dataExportTaskInfo = new DataExportTaskInfo();
        dataExportTaskInfo.id = 123L;
        DataExportTaskHistoryInfo dataExportTaskHistoryInfo = new DataExportTaskHistoryInfo();
        dataExportTaskHistoryInfo.id = 13L;
        dataExportTaskHistoryInfo.task  = dataExportTaskInfo;

        when(dataExportService.findExportTasks()).thenReturn(exportTaskFinder);
        when(dataExportService.getDataExportOccurrenceFinder()).thenReturn(dataExportOccurrenceFinder);
        when(exportTaskFinder.ofApplication(anyString())).thenReturn(exportTaskFinder);
        when(exportTaskFinder.stream()).thenReturn(queryStream);
        when(exportTask.getId()).thenReturn(123L);
        when(dataExportOccurrenceFinder.setLimit(anyInt())).thenReturn(dataExportOccurrenceFinder);
        when(dataExportOccurrenceFinder.setStart(anyInt())).thenReturn(dataExportOccurrenceFinder);
        when(dataExportOccurrenceFinder.stream()).thenReturn(queryStream);
        when(dataExportOccurrenceFinder.withExportTask(anyList())).thenReturn(dataExportOccurrenceFinder);
        when(queryStream.flatMap(any())).thenReturn(queryStream);
        when(queryStream.map(any())).thenReturn(queryStream);
        when(queryStream.collect(any())).thenReturn(Collections.singletonList(dataExportTaskHistoryInfo));

        Response response = target("/dataexporttask/history").request()
                .header(X_CONNEXO_APPLICATION_NAME, "INS")
                .header("start", 1)
                .header("limit", 1).get();

        JsonModel model = JsonModel.model((InputStream)response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(model.<Integer>get("$.data[0].id")).isEqualTo(13);
    }

    @Test
    public void testGetDataExportLogByOccurrence() {
        long occurrenceId = 13L;

        when(dataExportService.findDataExportOccurrence(anyLong())).thenReturn(Optional.of(occurrence));
        when(occurrence.getLogsFinder()).thenReturn(logEntryFinder);
        when(logEntryFinder.setStart(anyInt())).thenReturn(logEntryFinder);
        when(logEntryFinder.setLimit(anyInt())).thenReturn(logEntryFinder);
        when(logEntryFinder.find()).thenReturn(Collections.emptyList());

        Response response = target("/dataexporttask/history/" + occurrenceId + "/logs").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(dataExportService).findDataExportOccurrence(anyLong());
        verify(occurrence).getLogsFinder();
        verify(logEntryFinder).setStart(anyInt());
        verify(logEntryFinder).setLimit(anyInt());
    }

    @Test
    public void testGetDataExportOccurrence() {
        long occurrenceId = 13L;
        DataExportOccurrence dataExportOccurrence = mock(DataExportOccurrence.class);
        History<ExportTask> history = new History<>(Collections.emptyList(), null);
        when(dataExportService.findDataExportOccurrence(anyLong())).thenReturn(Optional.of(dataExportOccurrence));
        when(dataExportOccurrence.getTask()).thenReturn(exportTask);
        when(exportTask.getHistory()).thenReturn(history);
        when(exportTask.getStandardDataSelectorConfig(Instant.EPOCH)).thenReturn(Optional.empty());
        when(exportTask.getScheduleExpression(Instant.EPOCH)).thenReturn(Optional.empty());
        when(dataExportOccurrence.getId()).thenReturn(13L);
        when(dataExportOccurrence.getStartDate()).thenReturn(Optional.of(Instant.EPOCH));
        when(dataExportOccurrence.getEndDate()).thenReturn(Optional.of(Instant.EPOCH));
        when(dataExportOccurrence.getDefaultSelectorOccurrence()).thenReturn(Optional.empty());

        Response response = target("/dataexporttask/history/" + occurrenceId).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    private ReadingTypeDataExportItem mockExportItem(DataExportOccurrence dataExportOccurrence, IdentifiedObject domainObject, Instant lastRun) {
        ReadingTypeDataExportItem dataExportItem = mock(ReadingTypeDataExportItem.class);
        when(dataExportItem.isActive()).thenReturn(true);
        when(dataExportItem.getLastRun()).thenReturn(Optional.of(lastRun));
        doReturn(Optional.of(dataExportOccurrence)).when(dataExportItem).getLastOccurrence();
        when(dataExportItem.getDomainObject()).thenReturn(domainObject);
        ReadingType readingType = mockReadingType();
        when(dataExportItem.getReadingType()).thenReturn(readingType);
        when(dataExportItem.getLastExportedDate()).thenReturn(Optional.empty());
        return dataExportItem;
    }

    private IdentifiedObject mockMeter(String name, String serialNumber) {
        Meter meter = mock(Meter.class);
        when(meter.getName()).thenReturn(name);
        when(meter.getSerialNumber()).thenReturn(serialNumber);
        return meter;
    }

    private IdentifiedObject mockUsagePoint(String name, String connectionState) {
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getName()).thenReturn(name);
        when(usagePoint.getConnectionStateDisplayName()).thenReturn(connectionState);
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.of(ConnectionState.CONNECTED));
        return usagePoint;
    }

    public ReadingType mockReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        when(readingType.isCumulative()).thenReturn(true);
        return readingType;
    }
}
