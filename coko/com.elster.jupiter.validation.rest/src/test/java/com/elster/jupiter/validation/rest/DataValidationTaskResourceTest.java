package com.elster.jupiter.validation.rest;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationOccurrenceFinder;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataValidationTaskResourceTest extends BaseValidationRestTest {

    public static final int TASK_ID = 750;
    public static final String INSIGHT_KEY = "INS";
    public static final String MULTISENSE_KEY = "MDC";
    public static final String HEADER_NAME = "X-CONNEXO-APPLICATION-NAME";
    public static final long OK_VERSION = 23L;
    public static final long BAD_VERSION = 21L;

    @Mock
    protected EndDeviceGroup endDeviceGroup;

    DataValidationTaskBuilder taskBuilder;

    @Mock
    DataValidationTask dataValidationTask1;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        dataValidationTask1 = mockDataValidationTask(TASK_ID, QualityCodeSystem.MDC);
        taskBuilder = FakeBuilder.initBuilderStub(dataValidationTask1, DataValidationTaskBuilder.class);
        when(validationService.newTaskBuilder()).thenReturn(taskBuilder);
        when(validationService.findValidationTask(anyLong())).thenReturn(Optional.of(dataValidationTask1));
    }

    @Test
    public void getTasksTest() {
        mockDataValidationTasks(mockDataValidationTask(13, QualityCodeSystem.MDC), mockDataValidationTask(15, QualityCodeSystem.MDM));

        String jsonFromMultisense = target("/validationtasks").request().header(HEADER_NAME, MULTISENSE_KEY).get(String.class);

        JsonModel jsonModelFromMultisense = JsonModel.create(jsonFromMultisense);
        assertThat(jsonModelFromMultisense.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModelFromMultisense.<Number>get("$.dataValidationTasks[0].id")).isEqualTo(13);
        assertThat(jsonModelFromMultisense.<String>get("$.dataValidationTasks[0].name")).isEqualTo("Name");

        String jsonFromInsight = target("/validationtasks").request().header(HEADER_NAME, INSIGHT_KEY).get(String.class);

        JsonModel jsonModelFromInsight = JsonModel.create(jsonFromInsight);
        assertThat(jsonModelFromInsight.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModelFromInsight.<Number>get("$.dataValidationTasks[0].id")).isEqualTo(15);
        assertThat(jsonModelFromInsight.<String>get("$.dataValidationTasks[0].name")).isEqualTo("Name");
    }

    @Test
    public void getCreateTasksTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();//dataValidationTask1, thesaurus, timeService);
        info.deviceGroup = new IdWithDisplayValueInfo();
        info.deviceGroup.id = 1L;
        Entity<DataValidationTaskInfo> json = Entity.json(info);

        Response response = target("/validationtasks").request().header(HEADER_NAME, MULTISENSE_KEY).post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }
    
    @Test
    public void getCreateTasksMetrologyContractTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();//dataValidationTask1, thesaurus, timeService);
        info.deviceGroup = null;
        info.metrologyContract = new IdWithDisplayValueInfo();
        info.metrologyConfiguration = new IdWithDisplayValueInfo();
        info.metrologyContract.id = 1L;
        info.metrologyConfiguration.id = 1;
        Entity<DataValidationTaskInfo> json = Entity.json(info);

        Response response = target("/validationtasks").request().header(HEADER_NAME, INSIGHT_KEY).post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void updateTasksTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();//dataValidationTask1, thesaurus, timeService);
        info.id = TASK_ID;
        info.deviceGroup = new IdWithDisplayValueInfo();
        info.deviceGroup.id = 1L;

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().header(HEADER_NAME, MULTISENSE_KEY).put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
    
    @Test
    public void updateTasksMetrologyContractTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();//mockDataValidationTask(TASK_ID, QualityCodeSystem.MDM), thesaurus, timeService);
        info.id = TASK_ID;
        info.deviceGroup = null;
        info.metrologyContract = new IdWithDisplayValueInfo();
        info.metrologyConfiguration = new IdWithDisplayValueInfo();
        info.metrologyContract.id = 1L;
        info.metrologyConfiguration.id = 1;

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().header(HEADER_NAME, INSIGHT_KEY).put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void updateTasksTestBadVersion() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();//dataValidationTask1, thesaurus, timeService);
        info.id = TASK_ID;
        info.deviceGroup = new IdWithDisplayValueInfo();
        info.deviceGroup.id = 1L;
        info.version = BAD_VERSION;

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().header(HEADER_NAME, MULTISENSE_KEY).put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }
    
    @Test
    public void updateTasksTestBadMetrologyContractVersion() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();//dataValidationTask1, thesaurus, timeService);
        info.id = TASK_ID;
        info.deviceGroup = null;
        info.metrologyContract = new IdWithDisplayValueInfo();
        info.metrologyConfiguration = new IdWithDisplayValueInfo();
        info.metrologyContract.id = 1L;
        info.metrologyConfiguration.id = 1;
        info.version = BAD_VERSION;

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().header(HEADER_NAME, MULTISENSE_KEY).put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void deleteTasksTestBadVersion() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();//dataValidationTask1, thesaurus, timeService);
        info.id = TASK_ID;
        info.deviceGroup = new IdWithDisplayValueInfo();
        info.deviceGroup.id = 1L;
        info.version = BAD_VERSION;

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testHistorySearchWithInvertedStaredRange() throws Exception {

        Response response = target("/validationtasks/1/history")
                .queryParam("filter", ExtjsFilter.filter().property("startedOnFrom", 1441490400000L).property("startedOnTo", 1441058400000L).create())
                .queryParam("start", "0")
                .queryParam("limit", "10")
                .request()
                .header(HEADER_NAME, MULTISENSE_KEY)
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean> get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String> get("$.errors[0].id")).isEqualTo("startedOnFrom");
        assertThat(jsonModel.<String> get("$.errors[0].msg")).isEqualTo("Invalid range: from-date should be before to-date");
    }

    @Test
    public void testHistorySearchWithInvertedEndedRange() throws Exception {

        Response response = target("/validationtasks/1/history")
                .queryParam("filter", ExtjsFilter.filter().property("finishedOnFrom", 1441490400000L).property("finishedOnTo", 1441058400000L).create())
                .queryParam("start", "0")
                .queryParam("limit", "10")
                .request()
                .header(HEADER_NAME, MULTISENSE_KEY)
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean> get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String> get("$.errors[0].id")).isEqualTo("finishedOnFrom");
        assertThat(jsonModel.<String> get("$.errors[0].msg")).isEqualTo("Invalid range: from-date should be before to-date");
    }

    @Test
    public void testHistorySearchWithoutFrom() throws Exception {

        Response response = target("/validationtasks/1/history")
                .queryParam("filter", ExtjsFilter.filter().property("startedOnTo", 1441058400000L).create())
                .queryParam("start", "0")
                .queryParam("limit", "10")
                .request()
                .header(HEADER_NAME, MULTISENSE_KEY)
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    private void mockDataValidationTasks(DataValidationTask... validationTasks) {

        when(validationService.findValidationTasks()).thenReturn(Arrays.asList(validationTasks));
    }

    private DataValidationTask mockDataValidationTask(int id, QualityCodeSystem qualityCodeSystem) {
        Long lid = Long.valueOf(id);
        DataValidationTask validationTask = mock(DataValidationTask.class);
        when(validationTask.getId()).thenReturn(lid);
        when(validationTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(validationTask.getName()).thenReturn("Name");
        when(validationTask.getLastRun()).thenReturn(Optional.<Instant> empty());
        when(validationTask.getEndDeviceGroup()).thenReturn(Optional.of(endDeviceGroup));
        when(validationTask.getQualityCodeSystem()).thenReturn(qualityCodeSystem);
        when(validationTask.getMetrologyContract()).thenReturn(Optional.empty());
        DataValidationOccurrenceFinder finder = mock(DataValidationOccurrenceFinder.class);
        when(finder.setLimit(anyInt())).thenReturn(finder);
        when(finder.setStart(anyInt())).thenReturn(finder);
        when(validationTask.getOccurrencesFinder()).thenReturn(finder);
        when(validationTask.getLastOccurrence()).thenReturn(Optional.<DataValidationOccurrence> empty());
        when(validationTask.getVersion()).thenReturn(OK_VERSION);

        doReturn(Optional.of(validationTask)).when(validationService).findValidationTask(lid);
        doReturn(Optional.of(validationTask)).when(validationService).findAndLockValidationTaskByIdAndVersion(lid, OK_VERSION);
        doReturn(Optional.empty()).when(validationService).findAndLockValidationTaskByIdAndVersion(lid, BAD_VERSION);

        return validationTask;
    }
}
