package com.elster.jupiter.export.rest.impl;

import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class DataExportTaskResourceTest extends DataExportApplicationJerseyTest {

    public static final ZonedDateTime NEXT_EXECUTION = ZonedDateTime.of(2015, 1, 13, 0, 0, 0, 0, ZoneId.systemDefault());
    public static final int TASK_ID = 750;

    @Test
    public void getTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();

        Response response1 = target("/dataexporttask").request().get();
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        DataExportTaskInfos infos = response1.readEntity(DataExportTaskInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.dataExportTasks).hasSize(1);
    }

    @Test
    public void triggerTaskTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();

        Response response1 = target("/dataexporttask/"+TASK_ID+"/trigger").request().post(Entity.json(null));
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(exportTask).triggerNow();
    }


    @Test
    public void getCreateTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.name = "newName";
        info.nextRun = 250L;
        info.standardDataSelector = new StandardDataSelectorInfo();
        info.standardDataSelector.deviceGroup = new MeterGroupInfo();
        info.standardDataSelector.deviceGroup.id = 5;
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";

        Entity<DataExportTaskInfo> json = Entity.json(info);

        Response response = target("/dataexporttask").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void updateTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.standardDataSelector = new StandardDataSelectorInfo();
        info.standardDataSelector.deviceGroup = new MeterGroupInfo();
        info.standardDataSelector.deviceGroup.id = 5;
        info.dataProcessor = new ProcessorInfo();
        info.dataProcessor.name = "dataProcessor";

        Entity<DataExportTaskInfo> json = Entity.json(info);

        Response response = target("/dataexporttask/" + TASK_ID).request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }
}
