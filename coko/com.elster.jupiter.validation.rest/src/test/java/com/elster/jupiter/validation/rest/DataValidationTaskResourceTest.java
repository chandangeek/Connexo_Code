package com.elster.jupiter.validation.rest;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationOccurrenceFinder;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.*;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataValidationTaskResourceTest extends BaseValidationRestTest {

    public static final int TASK_ID = 750;

    @Mock
    protected EndDeviceGroup endDeviceGroup;

    @Mock
    DataValidationTaskBuilder taskBuilder;

    @Mock
    DataValidationTask dataValidationTask1;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        dataValidationTask1 = mockDataValidationTask(TASK_ID);
        when(taskBuilder.setName(Matchers.any())).thenReturn(taskBuilder);
        when(taskBuilder.setEndDeviceGroup(Matchers.any())).thenReturn(taskBuilder);
        when(taskBuilder.setScheduleExpression(Matchers.any())).thenReturn(taskBuilder);
        when(taskBuilder.setNextExecution(Matchers.any())).thenReturn(taskBuilder);
        when(taskBuilder.build()).thenReturn(dataValidationTask1);
        when(validationService.newTaskBuilder()).thenReturn(taskBuilder);
        when(validationService.findValidationTask(anyLong())).thenReturn(Optional.of(dataValidationTask1));
    }

    @Test
    public void getTasksTest() {
        mockDataValidationTasks(mockDataValidationTask(13));

        Response response1 = target("/validationtasks").request().get();
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        DataValidationTaskInfos infos = response1.readEntity(DataValidationTaskInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.dataValidationTasks).hasSize(1);
    }

    @Test
    public void getCreateTasksTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo(dataValidationTask1, thesaurus, timeService);
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 1;
        Entity<DataValidationTaskInfo> json = Entity.json(info);

        Response response = target("/validationtasks").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }


    @Test
    public void deleteTaskTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.name = "newName";
        info.lastRun = 250L;
    }

    @Test
    public void updateTasksTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo(dataValidationTask1, thesaurus, timeService);
        info.id = TASK_ID;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 1;

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testHistorySearchWithInvertedStaredRange() throws Exception {

        Response response = target("/validationtasks/1/history")
                .queryParam("filter", ExtjsFilter.filter().property("startedOnFrom",1441490400000L).property("startedOnTo", 1441058400000L).create())
                .queryParam("start", "0")
                .queryParam("limit", "10")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("startedOnFrom");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("Invalid range: from-date should be before to-date");
    }

    @Test
    public void testHistorySearchWithInvertedEndedRange() throws Exception {

        Response response = target("/validationtasks/1/history")
                .queryParam("filter", ExtjsFilter.filter().property("finishedOnFrom",1441490400000L).property("finishedOnTo", 1441058400000L).create())
                .queryParam("start", "0")
                .queryParam("limit", "10")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("finishedOnFrom");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("Invalid range: from-date should be before to-date");
    }

    @Test
    public void testHistorySearchWithoutFrom() throws Exception {

        Response response = target("/validationtasks/1/history")
                .queryParam("filter", ExtjsFilter.filter().property("startedOnTo", 1441058400000L).create())
                .queryParam("start", "0")
                .queryParam("limit", "10")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    private void mockDataValidationTasks(DataValidationTask... validationTasks) {
        Query<DataValidationTask> query = mock(Query.class);
        when(validationService.findValidationTasksQuery()).thenReturn(query);
        RestQuery<DataValidationTask> restQuery = mock(RestQuery.class);
        when(restQueryService.wrap(query)).thenReturn(restQuery);
        when(restQuery.select(any(QueryParameters.class), any(Order.class))).thenReturn(Arrays.asList(validationTasks));
    }

    private DataValidationTask mockDataValidationTask(int id) {
        DataValidationTask validationTask = mock(DataValidationTask.class);
        when(validationTask.getId()).thenReturn(Long.valueOf(id));
        when(validationTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(validationTask.getName()).thenReturn("Name");
        when(validationTask.getLastRun()).thenReturn(Optional.<Instant>empty());
        when(validationTask.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        DataValidationOccurrenceFinder finder = mock(DataValidationOccurrenceFinder.class);
        when(finder.setLimit(anyInt())).thenReturn(finder);
        when(finder.setStart(anyInt())).thenReturn(finder);
        when(validationTask.getOccurrencesFinder()).thenReturn(finder);
        when(validationTask.getLastOccurrence()).thenReturn(Optional.<DataValidationOccurrence>empty());
        doReturn(Optional.of(validationTask)).when(validationService).findValidationTask(id);

        return validationTask;
    }
}
