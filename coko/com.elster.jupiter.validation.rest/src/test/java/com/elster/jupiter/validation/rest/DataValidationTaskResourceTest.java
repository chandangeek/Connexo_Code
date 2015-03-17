package com.elster.jupiter.validation.rest;

import com.elster.jupiter.domain.util.Query;

import com.elster.jupiter.validation.DataValidationTask;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataValidationTaskResourceTest extends BaseValidationRestTest {

    public static final int TASK_ID = 750;

    @Test
    public void getTasksTest() {
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
        info.deviceGroup.id = 5;

        Entity<DataValidationTaskInfo> json = Entity.json(info);

        Response response = target("/datavalidationtasks").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void deleteCreateTasksTest() {
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



}
