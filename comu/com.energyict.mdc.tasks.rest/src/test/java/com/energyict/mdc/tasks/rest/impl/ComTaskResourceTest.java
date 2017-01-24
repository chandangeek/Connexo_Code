package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.rest.Categories;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by gde on 5/05/2015.
 */
public class ComTaskResourceTest extends ComTasksApplicationJerseyTest {

    public static final long OK_VERSION = 58L;
    public static final long BAD_VERSION = 43L;
    public static final long COM_TASK_ID = 17L;

    @Test
    public void testGetCategories() throws Exception {
        String response = target("/comtasks/categories").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(7);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='logbooks')].name[0]")).isEqualTo(MessageSeeds.LOGBOOKS.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='registers')].name[0]")).isEqualTo(MessageSeeds.REGISTERS.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='topology')].name[0]")).isEqualTo(MessageSeeds.TOPOLOGY.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='loadprofiles')].name[0]")).isEqualTo(MessageSeeds.LOADPROFILES.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='clock')].name[0]")).isEqualTo(MessageSeeds.CLOCK.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='statusInformation')].name[0]")).isEqualTo(MessageSeeds.STATUS_INFORMATION.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.data[?(@.id=='basiccheck')].name[0]")).isEqualTo(MessageSeeds.BASIC_CHECK.getDefaultFormat());
    }

    @Test
    public void testLoadProfilesActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.LOADPROFILES.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='read')].name[0]")).isEqualTo("Read");
    }

    @Test
    public void testLogbooksActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.LOGBOOKS.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='read')].name[0]")).isEqualTo("Read");
    }

    @Test
    public void testRegisterActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.REGISTERS.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='read')].name[0]")).isEqualTo("Read");
    }

    @Test
    public void testTopologyActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.TOPOLOGY.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List>get("$.data[*].id")).contains("update", "verify").hasSize(2);
        assertThat(jsonModel.<List>get("$.data[*].name")).contains("Update", "Verify").hasSize(2);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='update')].name[0]")).isEqualTo("Update");
    }

    @Test
    public void testClockActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.CLOCK.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List>get("$.data[*].id")).contains("set", "force", "synchronize").hasSize(3);
        assertThat(jsonModel.<List>get("$.data[*].name")).contains("Set", "Force", "Synchronize").hasSize(3);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='set')].name[0]")).isEqualTo("Set");
        assertThat(jsonModel.<String>get("$.data[?(@.id=='force')].name[0]")).isEqualTo("Force");
    }

    @Test
    public void testStatusInformationActions() throws Exception {
        String response = target("/comtasks/actions").queryParam("category", Categories.STATUSINFORMATION.getId()).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.data[?(@.id=='read')].name[0]")).isEqualTo("Read");
    }

    @Test
    public void testGetActionsForNonexistingCategory() throws Exception {
        final Response response = target("/comtasks/actions").queryParam("category", "Nonexisting").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetActionsWithoutCategory() throws Exception {
        final Response response = target("/comtasks/actions").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    private ComTask mockComTask() {
        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(COM_TASK_ID);
        when(comTask.getVersion()).thenReturn(OK_VERSION);
        when(comTask.getProtocolTasks()).thenReturn(new ArrayList<ProtocolTask>());
        when(taskService.findComTask(COM_TASK_ID)).thenReturn(Optional.of(comTask));
        when(taskService.findAndLockComTaskByIdAndVersion(COM_TASK_ID, OK_VERSION)).thenReturn(Optional.of(comTask));
        when(taskService.findAndLockComTaskByIdAndVersion(COM_TASK_ID, BAD_VERSION)).thenReturn(Optional.empty());
        return comTask;
    }


    @Test
    public void testUpdateComTaskOkVersion() {
        ComTask comTask = mockComTask();
        ComTaskInfo info = ComTaskInfo.from(comTask);
        info.name = "new name";
        info.version = OK_VERSION;
        info.commands = new ArrayList<>();
        Response response = target("/comtasks/" + COM_TASK_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTask, times(1)).setName("new name");
    }
    @Test
    public void testUpdateComTaskBadVersion() {
        ComTask comTask = mockComTask();
        ComTaskInfo info = ComTaskInfo.from(comTask);
        info.version = BAD_VERSION;
        Response response = target("/comtasks/" + COM_TASK_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(comTask, never()).setName("new name");
    }
    @Test
    public void testDeleteComTaskOkVersion() {
        ComTask comTask = mockComTask();
        ComTaskInfo info = ComTaskInfo.from(comTask);
        info.version = OK_VERSION;
        Response response = target("/comtasks/" + COM_TASK_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTask, times(1)).delete();
    }
    @Test
    public void testDeleteComTaskBadVersion() {
        ComTask comTask = mockComTask();
        ComTaskInfo info = ComTaskInfo.from(comTask);
        info.name = "new name";
        info.version = BAD_VERSION;
        Response response = target("/comtasks/" + COM_TASK_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(comTask, never()).delete();
    }
}
