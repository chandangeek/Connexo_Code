package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.SingleComTaskComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.jayway.jsonpath.JsonModel;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class ComTaskExecutionResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetManuallyScheduledComTaskExecutionsPaged() throws Exception {
        DeviceType deviceType = mockDeviceType(21, "Some type");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration);
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(manuallyScheduledComTaskExecution.getId()).thenReturn(102L);
        when(manuallyScheduledComTaskExecution.getExecutionPriority()).thenReturn(-20);
        when(manuallyScheduledComTaskExecution.getDevice()).thenReturn(device);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(manuallyScheduledComTaskExecution));
        Response response = target("/devices/SPE001/comtaskexecutions").queryParam("start",0).queryParam("limit",10).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devices/SPE001/comtaskexecutions?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(102);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(LinkInfo.REF_SELF);
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devices/SPE001/comtaskexecutions/102");
    }

    @Test
    public void testGetSingleManuallyScheduledComTaskExecutionWithFields() throws Exception {
        Instant now = Instant.now();
        Instant later = now.plusSeconds(1000);
        Instant end = later.plusSeconds(1000);
        DeviceType deviceType = mockDeviceType(21, "Some type");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration);
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(manuallyScheduledComTaskExecution.getId()).thenReturn(102L);
        when(manuallyScheduledComTaskExecution.getPlannedPriority()).thenReturn(-20);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(manuallyScheduledComTaskExecution));

        Response response = target("/devices/SPE001/comtaskexecutions/102").queryParam("fields","id,priority").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(102);
        assertThat(model.<Integer>get("$.priority")).isEqualTo(-20);
    }

    @Test
    public void testGetSingleManuallyScheduledComTaskExecution() throws Exception {
        Instant now = Instant.now();
        Instant later = now.plusSeconds(1000);
        Instant end = later.plusSeconds(1000);
        DeviceType deviceType = mockDeviceType(21, "Some type");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration);
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(manuallyScheduledComTaskExecution.getId()).thenReturn(102L);
        when(manuallyScheduledComTaskExecution.getPlannedPriority()).thenReturn(-20);
        ComTask comTask = mockComTask(23, "doIt");
        when(manuallyScheduledComTaskExecution.getDevice()).thenReturn(device);
        when(manuallyScheduledComTaskExecution.getComTask()).thenReturn(comTask);
        when(manuallyScheduledComTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));
        when(manuallyScheduledComTaskExecution.getNextExecutionTimestamp()).thenReturn(now);
        when(manuallyScheduledComTaskExecution.getPlannedNextExecutionTimestamp()).thenReturn(later);
        when(manuallyScheduledComTaskExecution.getLastExecutionStartTimestamp()).thenReturn(end);
        when(manuallyScheduledComTaskExecution.isOnHold()).thenReturn(true);
        when(manuallyScheduledComTaskExecution.isAdHoc()).thenReturn(false);
        when(manuallyScheduledComTaskExecution.isScheduledManually()).thenReturn(true);
        when(manuallyScheduledComTaskExecution.usesSharedSchedule()).thenReturn(false);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(manuallyScheduledComTaskExecution));

        Response response = target("/devices/SPE001/comtaskexecutions/102").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(102);
        assertThat(model.<Integer>get("$.priority")).isEqualTo(-20);
        assertThat(model.<Integer>get("$.schedule")).isNull();
        assertThat(model.<Long>get("$.nextExecution")).isEqualTo(now.toEpochMilli());
        assertThat(model.<Long>get("$.plannedNextExecution")).isEqualTo(later.toEpochMilli());
        assertThat(model.<Long>get("$.lastCommunicationStart")).isEqualTo(end.toEpochMilli());
        assertThat(model.<String>get("$.type")).isEqualTo(ComTaskExecutionType.ManualSchedule.name());
        assertThat(model.<List>get("$.comTasks")).hasSize(1);
        assertThat(model.<Integer>get("$.comTasks[0].id")).isEqualTo(23);
        assertThat(model.<String>get("$.comTasks[0].link.href")).isEqualTo("http://localhost:9998/comtasks/23");
    }

    @Test
    public void testGetSingleAdHocComTaskExecution() throws Exception {
        Instant now = Instant.now();
        Instant later = now.plusSeconds(1000);
        Instant end = later.plusSeconds(1000);
        DeviceType deviceType = mockDeviceType(21, "Some type");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration);
        SingleComTaskComTaskExecution comTaskExecution = mock(SingleComTaskComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(102L);
        when(comTaskExecution.getPlannedPriority()).thenReturn(-20);
        ComTask comTask = mockComTask(23, "doIt");
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));
        when(comTaskExecution.getNextExecutionTimestamp()).thenReturn(null);
        when(comTaskExecution.getPlannedNextExecutionTimestamp()).thenReturn(later);
        when(comTaskExecution.getLastExecutionStartTimestamp()).thenReturn(end);
        when(comTaskExecution.isOnHold()).thenReturn(true);
        when(comTaskExecution.isAdHoc()).thenReturn(true);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(false);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        Response response = target("/devices/SPE001/comtaskexecutions/102").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(102);
        assertThat(model.<Integer>get("$.priority")).isEqualTo(-20);
        assertThat(model.<Integer>get("$.schedule")).isNull();
        assertThat(model.<Long>get("$.nextExecution")).isNull();
        assertThat(model.<Long>get("$.plannedNextExecution")).isEqualTo(later.toEpochMilli());
        assertThat(model.<Long>get("$.lastCommunicationStart")).isEqualTo(end.toEpochMilli());
        assertThat(model.<String>get("$.type")).isEqualTo(ComTaskExecutionType.AdHoc.name());
        assertThat(model.<List>get("$.comTasks")).hasSize(1);
        assertThat(model.<Integer>get("$.comTasks[0].id")).isEqualTo(23);
        assertThat(model.<String>get("$.comTasks[0].link.href")).isEqualTo("http://localhost:9998/comtasks/23");
    }

    @Test
    public void testGetSharedScheduleComTaskExecution() throws Exception {
        Instant now = Instant.now();
        Instant later = now.plusSeconds(1000);
        Instant end = later.plusSeconds(1000);
        DeviceType deviceType = mockDeviceType(21, "Some type");
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration);
        ScheduledComTaskExecution comTaskExecution = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(102L);
        when(comTaskExecution.getPlannedPriority()).thenReturn(-20);
        ComTask comTask = mockComTask(23, "doIt");
        when(comTaskExecution.getDevice()).thenReturn(device);
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getId()).thenReturn(24L);
        when(comSchedule.getName()).thenReturn("Periodically");
        when(comTaskExecution.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));
        when(comTaskExecution.getNextExecutionTimestamp()).thenReturn(now);
        when(comTaskExecution.getPlannedNextExecutionTimestamp()).thenReturn(later);
        when(comTaskExecution.getLastExecutionStartTimestamp()).thenReturn(end);
        when(comTaskExecution.isOnHold()).thenReturn(true);
        when(comTaskExecution.isAdHoc()).thenReturn(false);
        when(comTaskExecution.isScheduledManually()).thenReturn(false);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(true);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        Response response = target("/devices/SPE001/comtaskexecutions/102").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(102);
        assertThat(model.<Integer>get("$.priority")).isEqualTo(-20);
        assertThat(model.<Integer>get("$.schedule.id")).isEqualTo(24);
        assertThat(model.<String>get("$.schedule.link.href")).isEqualTo("http://localhost:9998/comschedules/24");
        assertThat(model.<Long>get("$.nextExecution")).isEqualTo(now.toEpochMilli());
        assertThat(model.<Long>get("$.plannedNextExecution")).isEqualTo(later.toEpochMilli());
        assertThat(model.<Long>get("$.lastCommunicationStart")).isEqualTo(end.toEpochMilli());
        assertThat(model.<String>get("$.type")).isEqualTo(ComTaskExecutionType.SharedSchedule.name());
        assertThat(model.<List>get("$.comTasks")).hasSize(1);
        assertThat(model.<Integer>get("$.comTasks[0].id")).isEqualTo(23);
        assertThat(model.<String>get("$.comTasks[0].link.href")).isEqualTo("http://localhost:9998/comtasks/23");
    }

    @Test
    public void testComTaskExecutionFields() throws Exception {
        Response response = target("/devices/x/comtaskexecutions").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(13);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "comTasks", "schedule", "nextExecution", "plannedNextExecution",
                "priority", "type", "lastCommunicationStart", "status", "isOnHold", "lastSuccessfulCompletion", "device");
    }


}
