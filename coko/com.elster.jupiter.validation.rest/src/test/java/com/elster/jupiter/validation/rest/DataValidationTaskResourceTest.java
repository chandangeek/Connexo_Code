package com.elster.jupiter.validation.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationRuleSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataValidationTaskResourceTest extends BaseValidationRestTest {

    public static final int TASK_ID = 750;

    @Test
    public void testGetDataValidationTasks() {
        mockDataValidationTask(13, true);

        DataValidationTaskInfos infos = target("/datavalidationtasks").request().get(DataValidationTaskInfos.class);
    }

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
        //assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        //verify(readingTypeDataExportTask).triggerNow();
    }

    @Test
    public void getCreateTasksTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.name = "newName";
        //info.endDeviceGroup;

        //info.lastRun = Instant.MAX;

        //info.endDeviceGroup = new EndDe
        //info.deviceGroup.id = 5;
        //info.dataProcessor = new ProcessorInfo();
        //info.dataProcessor.name = "dataProcessor";

        Entity<DataValidationTaskInfo> json = Entity.json(info);

        Response response1 = target("/datavalidationtasks").request().post(json);
        assertThat(response1.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        //assertThat(true);
    }

    @Test
    public void deleteCreateTasksTest() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.name = "newName";
        //info.lastRun = 250L;

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

    private void mockDataValidationTask(int id, boolean addRules) {

        DataValidationTaskInfo taskInfo = mock(DataValidationTaskInfo.class);
        //when(taskInfo.getId()).thenReturn(Long.valueOf(id));
        //when(taskInfo.getName()).thenReturn("MyName");
        //when(taskInfo.getDescription()).thenReturn("MyDescription");

        Query<DataValidationTask> query = mock(Query.class);
        //when(validationService.get()).thenReturn(query);
        RestQuery<ValidationRuleSet> restQuery = mock(RestQuery.class);
        //when(restQueryService.wrap(query)).thenReturn(restQuery);
        //when(restQuery.select(any(QueryParameters.class), any(Order.class))).thenReturn(Arrays.asList(validationRuleSets));
    }

    private DataValidationTaskInfo mockDataValidationTaskInfo(int id, boolean addRules) {
        DataValidationTaskInfo taskInfo = mock(DataValidationTaskInfo.class);
        //when(ruleSet.getId()).thenReturn(Long.valueOf(id));
        //when(ruleSet.getName()).thenReturn("MyName");
        //when(ruleSet.getDescription()).thenReturn("MyDescription");
/*
        if (addRules) {
            List rules = Arrays.asList(mockValidationRuleInRuleSet(1L, ruleSet));
            when(ruleSet.getRules()).thenReturn(rules);
        }

        doReturn(Optional.of(ruleSet)).when(validationService).getValidationRuleSet(id);
*/
        return taskInfo;
    }


}
