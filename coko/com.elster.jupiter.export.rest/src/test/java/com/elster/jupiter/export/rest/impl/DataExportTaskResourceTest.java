package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
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
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ExportTaskFinder;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.StandardDataSelector;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.time.Never;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
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
import static org.mockito.Matchers.anyString;
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
    protected StandardDataSelector standardDataSelector;
    @Mock
    private DataSelectorFactory dataSelectorFactory;
    @Mock
    private DataFormatterFactory dataFormatterFactory;
    @Mock
    protected DataExportOccurrenceFinder dataExportOccurrenceFinder;
    @Mock
    protected EndDeviceGroup endDeviceGroup;
    @Mock
    protected RelativePeriod exportPeriod;
    @Mock
    protected DataExportStrategy strategy;
    @Mock
    protected QueryExecutor<DataExportOccurrence> queryExecutor;
    @Mock
    private FileDestination newDestination;
    @Mock
    private ExportTaskFinder exportTaskFinder;

    private DataExportTaskBuilder builder;


    @Before
    public void setUpMocks() {
        builder = FakeBuilder.initBuilderStub(exportTask, DataExportTaskBuilder.class,
                DataExportTaskBuilder.PropertyBuilder.class,
                DataExportTaskBuilder.CustomSelectorBuilder.class,
                DataExportTaskBuilder.EventSelectorBuilder.class
        );
        when(transactionService.execute(any())).thenAnswer(invocation -> ((Transaction<?>) invocation.getArguments()[0]).perform());
        when(dataExportService.findExportTasks()).thenReturn(exportTaskFinder);
        doReturn(Optional.of(standardDataSelector)).when(exportTask).getReadingTypeDataSelector();
        when(standardDataSelector.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        when(standardDataSelector.getExportPeriod()).thenReturn(exportPeriod);
        when(exportPeriod.getRelativeDateFrom()).thenReturn(new RelativeDate(RelativeField.DAY.minus(1)));
        when(exportPeriod.getRelativeDateTo()).thenReturn(new RelativeDate());
        when(standardDataSelector.getStrategy()).thenReturn(strategy);
        when(strategy.getUpdateWindow()).thenReturn(Optional.empty());
        when(standardDataSelector.getStrategy().getUpdatePeriod()).thenReturn(Optional.of(exportPeriod));
        when(exportTask.getNextExecution()).thenReturn(NEXT_EXECUTION.toInstant());
        when(meteringGroupsService.findEndDeviceGroup(5)).thenReturn(Optional.of(endDeviceGroup));
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
    public void geTask() {
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
        info.standardDataSelector.deviceGroup = new MeterGroupInfo();
        info.standardDataSelector.deviceGroup.id = 5;
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";
        info.dataSelector = new SelectorInfo();
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
        info.standardDataSelector.deviceGroup = new MeterGroupInfo();
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
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
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
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

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
    public void getDataSources() {
        Instant lastRun = Instant.now();
        DataExportOccurrence dataExportOccurrence = mock(DataExportOccurrence.class);
        when(dataExportOccurrence.getId()).thenReturn(13L);
        List<ReadingTypeDataExportItem> items = Arrays.asList(
                mockExportItem(dataExportOccurrence, mockMeterReadingContainer("SPE001", "001", lastRun), lastRun),
                mockExportItem(dataExportOccurrence, mockMeterReadingContainer("SPE002", "002", lastRun), lastRun),
                mockExportItem(dataExportOccurrence, mockMeterReadingContainer("SPE003", "003", lastRun), lastRun)
        );
        doReturn(items).when(standardDataSelector).getExportItems();

        // Business method
        String response = target("/dataexporttask/" + TASK_ID + "/datasources")
                .queryParam("start", 1).queryParam("limit", 1).request().header(X_CONNEXO_APPLICATION_NAME, "MDC").get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<?>>get("$.dataSources")).hasSize(1);
        assertThat(jsonModel.<String>get("$.dataSources[0].mRID")).isEqualTo("SPE002");
        assertThat(jsonModel.<String>get("$.dataSources[0].serialNumber")).isEqualTo("002");
        assertThat(jsonModel.<String>get("$.dataSources[0].readingType.mRID")).isEqualTo("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        assertThat(jsonModel.<Number>get("$.dataSources[0].occurrenceId")).isEqualTo(13);
    }

    private ReadingTypeDataExportItem mockExportItem(DataExportOccurrence dataExportOccurrence, ReadingContainer readingContainer, Instant lastRun) {
        ReadingTypeDataExportItem dataExportItem = mock(ReadingTypeDataExportItem.class);
        when(dataExportItem.isActive()).thenReturn(true);
        when(dataExportItem.getLastRun()).thenReturn(Optional.of(lastRun));
        doReturn(Optional.of(dataExportOccurrence)).when(dataExportItem).getLastOccurrence();
        when(dataExportItem.getReadingContainer()).thenReturn(readingContainer);
        ReadingType readingType = mockReadingType();
        when(dataExportItem.getReadingType()).thenReturn(readingType);
        when(dataExportItem.getLastExportedDate()).thenReturn(Optional.empty());
        return dataExportItem;
    }

    private ReadingContainer mockMeterReadingContainer(String name, String serialNumber, Instant instant) {
        Meter meter = mock(Meter.class);
        when(meter.getMeter(instant)).thenReturn(Optional.of(meter));
        when(meter.getMRID()).thenReturn(name);
        when(meter.getSerialNumber()).thenReturn(serialNumber);
        return meter;
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
