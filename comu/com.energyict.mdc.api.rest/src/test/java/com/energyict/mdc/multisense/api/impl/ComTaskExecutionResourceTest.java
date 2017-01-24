package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComTaskExecutionResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetManuallyScheduledComTaskExecutionsPaged() throws Exception {
        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration, 3333L);
        ComTaskExecution manuallyScheduledComTaskExecution = mock(ComTaskExecution.class);
        ComTask comTask = mockComTask(23, "Com task", 3333L);

        when(manuallyScheduledComTaskExecution.getId()).thenReturn(102L);
        when(manuallyScheduledComTaskExecution.getExecutionPriority()).thenReturn(-20);
        when(manuallyScheduledComTaskExecution.getDevice()).thenReturn(device);
        when(manuallyScheduledComTaskExecution.getConnectionTask()).thenReturn(Optional.empty());
        when(manuallyScheduledComTaskExecution.getComTask()).thenReturn(comTask);
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
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devices/SPE001/comtaskexecutions/102");
    }

    @Test
    public void testGetSingleManuallyScheduledComTaskExecutionWithFields() throws Exception {
        Instant now = Instant.now();
        Instant later = now.plusSeconds(1000);
        Instant end = later.plusSeconds(1000);
        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration, 3333L);
        ComTaskExecution manuallyScheduledComTaskExecution = mock(ComTaskExecution.class);
        when(manuallyScheduledComTaskExecution.getId()).thenReturn(102L);
        when(manuallyScheduledComTaskExecution.getPlannedPriority()).thenReturn(-20);
        when(manuallyScheduledComTaskExecution.getDevice()).thenReturn(device);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(manuallyScheduledComTaskExecution));

        Response response = target("/devices/SPE001/comtaskexecutions/102").queryParam("fields","id,priority").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(102);
        assertThat(model.<Integer>get("$.device.id")).isEqualTo(-1842762839);
        assertThat(model.<Integer>get("$.priority")).isEqualTo(-20);
        assertThat(model.<Integer>get("$.version")).isNull();
    }

    @Test
    public void testGetSingleManuallyScheduledComTaskExecution() throws Exception {
        Instant now = Instant.now();
        Instant later = now.plusSeconds(1000);
        Instant end = later.plusSeconds(1000);
        DeviceType deviceType = mockDeviceType(21, "Some type", 13333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration, 23333L);
        ComTaskExecution manuallyScheduledComTaskExecution = mock(ComTaskExecution.class);
        when(manuallyScheduledComTaskExecution.getId()).thenReturn(102L);
        when(manuallyScheduledComTaskExecution.getPlannedPriority()).thenReturn(-20);
        ComTask comTask = mockComTask(23, "doIt", 33333L);
        when(manuallyScheduledComTaskExecution.getDevice()).thenReturn(device);
        when(manuallyScheduledComTaskExecution.getComTask()).thenReturn(comTask);
        when(manuallyScheduledComTaskExecution.getNextExecutionTimestamp()).thenReturn(now);
        when(manuallyScheduledComTaskExecution.getPlannedNextExecutionTimestamp()).thenReturn(later);
        when(manuallyScheduledComTaskExecution.getLastExecutionStartTimestamp()).thenReturn(end);
        when(manuallyScheduledComTaskExecution.isOnHold()).thenReturn(true);
        when(manuallyScheduledComTaskExecution.isAdHoc()).thenReturn(false);
        when(manuallyScheduledComTaskExecution.isScheduledManually()).thenReturn(true);
        when(manuallyScheduledComTaskExecution.usesSharedSchedule()).thenReturn(false);
        when(manuallyScheduledComTaskExecution.getVersion()).thenReturn(1111L);
        ScheduledConnectionTask scheduledConnectionTask = mockScheduledConnectionTask(24, "Scheduled task", 43333L);
        when(manuallyScheduledComTaskExecution.getConnectionTask()).thenReturn(Optional.of(scheduledConnectionTask));
        when(scheduledConnectionTask.getDevice()).thenReturn(device);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(manuallyScheduledComTaskExecution));

        Response response = target("/devices/SPE001/comtaskexecutions/102").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(102);
        assertThat(model.<Integer>get("$.priority")).isEqualTo(-20);
        assertThat(model.<Integer>get("$.version")).isEqualTo(1111);
        assertThat(model.<Integer>get("$.schedule")).isNull();
        assertThat(model.<Long>get("$.nextExecution")).isEqualTo(now.toEpochMilli());
        assertThat(model.<Long>get("$.plannedNextExecution")).isEqualTo(later.toEpochMilli());
        assertThat(model.<Long>get("$.lastCommunicationStart")).isEqualTo(end.toEpochMilli());
        assertThat(model.<String>get("$.type")).isEqualTo(ComTaskExecutionType.ManualSchedule.name());
        assertThat(model.<Integer>get("$.comTask.id")).isEqualTo(23);
        assertThat(model.<String>get("$.comTask.link.href")).isEqualTo("http://localhost:9998/comtasks/23");
        assertThat(model.<Integer>get("$.device.version")).isEqualTo(23333);
        assertThat(model.<String>get("$.comTask.link.href")).isEqualTo("http://localhost:9998/comtasks/23");
        assertThat(model.<Integer>get("$.connectionTask.id")).isEqualTo(24);
        assertThat(model.<String>get("$.connectionTask.link.href")).isEqualTo("http://localhost:9998/devices/SPE001/connectiontasks/24");
    }

    @Test
    public void testGetSingleAdHocComTaskExecution() throws Exception {
        Instant now = Instant.now();
        Instant later = now.plusSeconds(1000);
        Instant end = later.plusSeconds(1000);
        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration, 3333L);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(102L);
        when(comTaskExecution.getPlannedPriority()).thenReturn(-20);
        ComTask comTask = mockComTask(23, "doIt", 3333L);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getNextExecutionTimestamp()).thenReturn(null);
        when(comTaskExecution.getPlannedNextExecutionTimestamp()).thenReturn(later);
        when(comTaskExecution.getLastExecutionStartTimestamp()).thenReturn(end);
        when(comTaskExecution.isOnHold()).thenReturn(true);
        when(comTaskExecution.isAdHoc()).thenReturn(true);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(false);
        ScheduledConnectionTask scheduledConnectionTask = mockScheduledConnectionTask(24, "Scheduled task", 3333L);
        when(scheduledConnectionTask.getDevice()).thenReturn(device);
        when(comTaskExecution.getConnectionTask()).thenReturn(Optional.of(scheduledConnectionTask));
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
        assertThat(model.<Integer>get("$.comTask.id")).isEqualTo(23);
        assertThat(model.<String>get("$.comTask.link.href")).isEqualTo("http://localhost:9998/comtasks/23");
        assertThat(model.<Integer>get("$.connectionTask.id")).isEqualTo(24);
        assertThat(model.<String>get("$.connectionTask.link.href")).isEqualTo("http://localhost:9998/devices/SPE001/connectiontasks/24");
    }

    @Test
    public void testGetSharedScheduleComTaskExecution() throws Exception {
        Instant now = Instant.now();
        Instant later = now.plusSeconds(1000);
        Instant end = later.plusSeconds(1000);
        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration, 3333L);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(102L);
        when(comTaskExecution.getPlannedPriority()).thenReturn(-20);
        ComTask comTask = mockComTask(23, "doIt", 3333L);
        when(comTaskExecution.getDevice()).thenReturn(device);
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getId()).thenReturn(24L);
        when(comSchedule.getName()).thenReturn("Periodically");
        when(comTaskExecution.getComSchedule()).thenReturn(Optional.of(comSchedule));
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getNextExecutionTimestamp()).thenReturn(now);
        when(comTaskExecution.getPlannedNextExecutionTimestamp()).thenReturn(later);
        when(comTaskExecution.getLastExecutionStartTimestamp()).thenReturn(end);
        when(comTaskExecution.isOnHold()).thenReturn(true);
        when(comTaskExecution.isAdHoc()).thenReturn(false);
        when(comTaskExecution.isScheduledManually()).thenReturn(false);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(true);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ScheduledConnectionTask scheduledConnectionTask = mockScheduledConnectionTask(25, "Scheduled task", 3333L);
        when(scheduledConnectionTask.getDevice()).thenReturn(device);
        when(comTaskExecution.getConnectionTask()).thenReturn(Optional.of(scheduledConnectionTask));

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
        assertThat(model.<Object>get("$.comTask")).isNotNull();
        assertThat(model.<Integer>get("$.connectionTask.id")).isEqualTo(25);
        assertThat(model.<String>get("$.connectionTask.link.href")).isEqualTo("http://localhost:9998/devices/SPE001/connectiontasks/25");
    }

    @Test
    public void testCreateAdHocComTaskExecutionDoesNotSupportSchedule() throws Exception {
        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        info.connectionTask = new LinkInfo();
        info.connectionTask.id = 23L;
        info.schedulingSpec = new TemporalExpressionInfo();
        info.schedulingSpec.every = new TimeDurationInfo();
        info.schedulingSpec.every.count = 1;
        info.schedulingSpec.every.timeUnit = "hours";
        info.comTask = new LinkInfo();
        info.comTask.id = 24L;
        info.type = ComTaskExecutionType.AdHoc;
        info.device = new LinkInfo();
        info.device.version = 3334L;

        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        mockDevice("SPE001", "01011", deviceConfiguration, 3334L);

        Response response = target("/devices/SPE001/comtaskexecutions").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateAdHocComTaskExecution() throws Exception {
        long comTaskId = 24;
        long connectionTaskId = 23L;

        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        info.connectionTask = new LinkInfo();
        info.connectionTask.id = connectionTaskId;
        info.comTask = new LinkInfo();
        info.comTask.id = comTaskId;
        info.type = ComTaskExecutionType.AdHoc;
        info.device = new LinkInfo();
        info.device.version = 3334L;

        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration, 3334L);
        ComTask comTask = mockComTask(comTaskId, "Com task", 3333L);
        ComTaskEnablement comTaskEnablement = mockComTaskEnablement(comTask, deviceConfiguration, 3333L);
        mockScheduledConnectionTask(connectionTaskId, "conntask", 3333L);
        when(deviceConfiguration.getComTaskEnablementFor(comTask)).thenReturn(Optional.of(comTaskEnablement));
        ComTaskExecutionBuilder builder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(comTaskEnablement)).thenReturn(builder);
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getId()).thenReturn(999L);
        ComTaskExecution comTaskExecution = comTaskExecution1;
        when(builder.add()).thenReturn(comTaskExecution);
        Response response = target("/devices/SPE001/comtaskexecutions").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getHeaderString("location")).isEqualTo("http://localhost:9998/devices/SPE001/comtaskexecutions/999");
        verify(device).newAdHocComTaskExecution(comTaskEnablement);
        verify(builder).add();
    }

    @Test
    public void testCreateScheduledComTaskExecution() throws Exception {
        long comTaskId = 24;
        long scheduleId = 24L;

        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        info.schedule = new LinkInfo();
        info.schedule.id = scheduleId;
        info.device = new LinkInfo();
        info.device.version = 3334L;
        info.useDefaultConnectionTask = true;
        info.type = ComTaskExecutionType.SharedSchedule;

        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        ComSchedule comSchedule = mockComSchedule(scheduleId, "Some schedule", 3333L);

        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration, 3334L);
        ComTask comTask = mockComTask(comTaskId, "Com task", 3333L);
        ComTaskEnablement comTaskEnablement = mockComTaskEnablement(comTask, deviceConfiguration, 3333L);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        when(comSchedule.getComTasks()).thenReturn(Collections.singletonList(comTask));
        when(deviceConfiguration.getComTaskEnablementFor(comTask)).thenReturn(Optional.of(comTaskEnablement));
        ComTaskExecutionBuilder builder = mock(ComTaskExecutionBuilder.class);
        when(device.newScheduledComTaskExecution(comSchedule)).thenReturn(builder);
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getId()).thenReturn(999L);
        ComTaskExecution comTaskExecution = comTaskExecution1;
        when(builder.add()).thenReturn(comTaskExecution);
        Response response = target("/devices/SPE001/comtaskexecutions").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getHeaderString("location")).isEqualTo("http://localhost:9998/devices/SPE001/comtaskexecutions/999");
        verify(device).newScheduledComTaskExecution(comSchedule);
        verify(builder).add();
    }

    @Test
    public void testUpdateScheduledComTaskExecution() throws Exception {
        long comTaskId = 24;
        long scheduleId = 24L;

        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        info.version = 3339L;
        info.schedule = new LinkInfo();
        info.schedule.id = scheduleId;
        info.useDefaultConnectionTask = true;
        info.type = ComTaskExecutionType.SharedSchedule;
        info.device = new LinkInfo();
        info.device.version = 3334L;

        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        ComSchedule comSchedule = mockComSchedule(scheduleId, "Some schedule", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration, 3334L);
        ComTask comTask = mockComTask(comTaskId, "Com task", 3333L);
        ComTaskEnablement comTaskEnablement = mockComTaskEnablement(comTask, deviceConfiguration, 3335L);


        ComTaskExecution scheduledComTaskExecution = mockScheduledComTaskExecution(999L, comSchedule, device, 3339L);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(scheduledComTaskExecution));
        when(deviceConfiguration.getComTaskEnablementFor(comTask)).thenReturn(Optional.of(comTaskEnablement));
        ComTaskExecutionUpdater updater = mock(ComTaskExecutionUpdater.class);
        when(scheduledComTaskExecution.getUpdater()).thenReturn(updater);
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getId()).thenReturn(999L);
        when(updater.update()).thenReturn(comTaskExecution1);

        Response response = target("/devices/SPE001/comtaskexecutions/999").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(updater).update();
    }

    @Test
    public void testUpdateScheduledComTaskExecutionVersionConflict() throws Exception {
        long comTaskId = 24;
        long scheduleId = 24L;

        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        info.version = 666L; // wrong version
        info.schedule = new LinkInfo();
        info.schedule.id = scheduleId;
        info.useDefaultConnectionTask = true;
        info.type = ComTaskExecutionType.SharedSchedule;
        info.device = new LinkInfo();
        info.device.version = 3334L;

        DeviceType deviceType = mockDeviceType(21, "Some type", 3333L);
        ComSchedule comSchedule = mockComSchedule(scheduleId, "Some schedule", 3333L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(22, "Default", deviceType, 3333L);
        Device device = mockDevice("SPE001", "01011", deviceConfiguration, 3334L);
        ComTask comTask = mockComTask(comTaskId, "Com task", 3333L);
        ComTaskEnablement comTaskEnablement = mockComTaskEnablement(comTask, deviceConfiguration, 3335L);
        when(communicationTaskService.findAndLockComTaskExecutionByIdAndVersion(999, 666)).thenReturn(Optional.empty());


        ComTaskExecution scheduledComTaskExecution = mockScheduledComTaskExecution(999L, comSchedule, device, 3339L);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(scheduledComTaskExecution));
        when(deviceConfiguration.getComTaskEnablementFor(comTask)).thenReturn(Optional.of(comTaskEnablement));
        ComTaskExecutionUpdater updater = mock(ComTaskExecutionUpdater.class);
        when(scheduledComTaskExecution.getUpdater()).thenReturn(updater);
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getId()).thenReturn(999L);
        when(updater.update()).thenReturn(comTaskExecution1);

        Response response = target("/devices/SPE001/comtaskexecutions/999").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(updater, never()).update();
    }

    @Test
    public void testComTaskExecutionFields() throws Exception {
        Response response = target("/devices/x/comtaskexecutions").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(15);
        assertThat(model.<List<String>>get("$")).containsOnly("id","version", "link", "comTask", "schedule", "nextExecution", "plannedNextExecution",
                "priority", "type", "lastCommunicationStart", "status", "lastSuccessfulCompletion", "device", "connectionTask",
                "ignoreNextExecutionSpecForInbound");
    }


}
