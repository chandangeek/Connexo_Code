package com.elster.jupiter.validation.rest;

import com.elster.jupiter.domain.util.Query;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.validation.DataValidationTask;

import com.elster.jupiter.validation.ValidationRuleSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataValidationTaskResourceTest extends BaseValidationRestTest {

    public static final ZonedDateTime NEXT_EXECUTION = ZonedDateTime.of(2015, 1, 13, 0, 0, 0, 0, ZoneId.systemDefault());
    public static final int TASK_ID = 750;



    @Mock
    protected EndDeviceGroup endDeviceGroup;

    @Test
    public void getTasksTest() {

        mockDataValidationTasks(mockDataValidationTask(13));

        DataValidationTaskInfo info = new DataValidationTaskInfo();

        Response response1 = target("/datavalidationtasks").request().get();
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        DataValidationTaskInfos infos = response1.readEntity(DataValidationTaskInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.dataValidationTasks).hasSize(1);
    }
    @Test
    public void triggerTaskTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();

        Response response1 = target("/datavalidationtasks/"+TASK_ID+"/trigger").request().post(Entity.json(null));
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(dataValidationTask).triggerNow();
    }

    @Test
    public void getCreateTasksTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.name = "newName";
        info.nextRun = 250L;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 1;
        info.deviceGroup.name = "Group1";

        Entity<DataValidationTaskInfo> json = Entity.json(info);

        Response response = target("/datavalidationtasks").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void deleteTaskTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.name = "newName";
        info.lastRun = 250L;

        //DataValidationTaskInfo validationRuleSet = mockDataValidationTaskInfo(99, false);
        //Response response = target("/datavalidationtasks/99").request().delete();
        //assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        //verify(validationRuleSet).delete();

        //info.deviceGroup = new MeterGroupInfo();
        //info.deviceGroup.id = 5;
        //info.dataProcessor = new ProcessorInfo();
        //info.dataProcessor.name = "dataProcessor";

        //Entity<DataValidationTaskInfo> json = Entity.json(info);

        //Response response = target("/datavalidationtasks").request().delete(json);

        //assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void updateTasksTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.id = TASK_ID;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 5;

        Entity<DataValidationTaskInfo> json = Entity.json(info);

        Response response = target("/datavalidationtasks/" + TASK_ID).request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
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
        doReturn(Optional.of(validationTask)).when(validationService).findValidationTask(id);

        return validationTask;

        }
}
