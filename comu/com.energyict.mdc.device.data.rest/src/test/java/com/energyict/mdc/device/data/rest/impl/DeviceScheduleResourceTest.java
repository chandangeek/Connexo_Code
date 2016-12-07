package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceScheduleResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void testGetComTaskExecutionsForEnablement() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mock(ComTask.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getPartialConnectionTask()).thenReturn(Optional.empty());
        Map<String, Object> response = target("/devices/1/schedules").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("schedules");
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) response.get("schedules");
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0))
                .containsKey("id")
                .containsKey("comTask")
                .containsKey("type");
    }

    @Test
    public void testGetComTaskExecutionsForEnablementWhichHasAdHocExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mock(ComTask.class);

        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.isAdHoc()).thenReturn(true);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getConnectionTask()).thenReturn(Optional.empty());

        Map<String, Object> response = target("/devices/1/schedules").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("schedules");
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) response.get("schedules");
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).get("type")).isEqualTo("ADHOC");
    }

    @Test
    public void testGetComTaskExecutionsForEnablementWhichHasManualExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mock(ComTask.class);

        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getConnectionTask()).thenReturn(Optional.empty());

        Map<String, Object> response = target("/devices/1/schedules").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("schedules");
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) response.get("schedules");
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).get("type")).isEqualTo("INDIVIDUAL");
    }

    @Test
    public void testGetComTaskExecutionsForEnablementWhichHasScheduledExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mock(ComTask.class);

        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.usesSharedSchedule()).thenReturn(true);
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getName()).thenReturn("com schedule");
        when(comTaskExecution.getComSchedule()).thenReturn(Optional.of(comSchedule));
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));
        when(comTaskExecution.getPlannedNextExecutionTimestamp()).thenReturn(Instant.now());
        when(comTaskExecution.getNextExecutionTimestamp()).thenReturn(Instant.now());
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(comTaskExecution.getConnectionTask()).thenReturn(Optional.empty());

        Map<String, Object> response = target("/devices/1/schedules").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("schedules");
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) response.get("schedules");
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).get("type")).isEqualTo("SCHEDULED");
        assertThat(schedules.get(0))
                .containsKey("name")
                .containsKey("id")
                .containsKey("masterScheduleId")
                .containsKey("schedule")
                .containsKey("plannedDate")
                .containsKey("nextCommunication")
                .containsKey("comTask")
                .containsKey("type");
    }

    private ComTask mockComTask(ComTaskEnablement comTaskEnablement, long comTaskId) {
        ComTask comTask = mock(ComTask.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTask.getId()).thenReturn(comTaskId);
        when(taskService.findComTask(comTaskId)).thenReturn(Optional.of(comTask));
        return comTask;
    }

    @Test
    public void testCreateScheduledComTaskExecutionFromEnablement() throws Exception {
        DeviceSchedulesInfo schedulingInfo = new DeviceSchedulesInfo();
        long comTaskId = 111L;
        schedulingInfo.id = comTaskId;
        schedulingInfo.schedule = TemporalExpressionInfo.from(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        mockComTask(comTaskEnablement, comTaskId);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getComTask()).thenReturn(null);
        when(communicationTaskService.findComTaskExecution(112L)).thenReturn(Optional.of(comTaskExecution));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newManuallyScheduledComTaskExecution(comTaskEnablement, schedulingInfo.schedule.asTemporalExpression())).thenReturn(comTaskExecutionBuilder);

        Response response = target("/devices/1/schedules").request().post(Entity.json(schedulingInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(comTaskExecutionBuilder, times(1)).add();
        verify(device, times(1)).newManuallyScheduledComTaskExecution(comTaskEnablement, schedulingInfo.schedule.asTemporalExpression());
    }

    @Test
    public void testChangeScheduleOnManuallyScheduledComTaskExecution() throws Exception {
        long comTaskId = 111;
        DeviceSchedulesInfo schedulingInfo = new DeviceSchedulesInfo();
        schedulingInfo.id = comTaskId;
        schedulingInfo.schedule = TemporalExpressionInfo.from(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));
        schedulingInfo.version = 1L;
        schedulingInfo.parent = new VersionInfo<>("1", 1L);


        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("1", 1L)).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mockComTask(comTaskEnablement, comTaskId);
        when(comTaskExecution.getId()).thenReturn(111L);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));

        ComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ComTaskExecutionUpdater.class);
        when(comTaskExecution.getUpdater()).thenReturn(scheduledComTaskExecutionUpdater);
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(scheduledComTaskExecutionUpdater.createNextExecutionSpecs(schedulingInfo.schedule.asTemporalExpression())).thenReturn(scheduledComTaskExecutionUpdater);
        when(communicationTaskService.findComTaskExecution(comTaskExecution.getId())).thenReturn(Optional.of(comTaskExecution));
        when(communicationTaskService.findAndLockComTaskExecutionByIdAndVersion(comTaskExecution.getId(), 1L)).thenReturn(Optional.of(comTaskExecution));

        Response response = target("/devices/1/schedules").request().put(Entity.json(schedulingInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(scheduledComTaskExecutionUpdater, times(1)).createNextExecutionSpecs(schedulingInfo.schedule.asTemporalExpression());
        verify(scheduledComTaskExecutionUpdater, times(1)).update();
    }

    @Test
    public void testRemoveScheduleOnManuallyScheduledComTaskExecution() throws Exception {
        long comTaskId = 11L;
        long comTaskExecutionId = 12L;
        DeviceSchedulesInfo info = new DeviceSchedulesInfo();
        info.id = comTaskExecutionId;
        info.version = 1L;
        info.parent = new VersionInfo<>("1", 1L);

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("1", 1L)).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mockComTask(comTaskEnablement, comTaskId);
        when(comTaskExecution.getId()).thenReturn(comTaskExecutionId);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.isScheduledManually()).thenReturn(true);
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(communicationTaskService.findAndLockComTaskExecutionByIdAndVersion(comTaskExecutionId, 1L)).thenReturn(Optional.of(comTaskExecution));
        when(communicationTaskService.findComTaskExecution(comTaskExecutionId)).thenReturn(Optional.of(comTaskExecution));

        ComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ComTaskExecutionUpdater.class);
        when(comTaskExecution.getUpdater()).thenReturn(scheduledComTaskExecutionUpdater);
        when(scheduledComTaskExecutionUpdater.removeSchedule()).thenReturn(scheduledComTaskExecutionUpdater);

        Response response = target("/devices/1/schedules").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(scheduledComTaskExecutionUpdater, times(1)).removeSchedule();
    }

    private DeviceSchedulesInfo mockDataForFirmwareComTaskTests() {
        DeviceSchedulesInfo schedulingInfo = new DeviceSchedulesInfo();
        schedulingInfo.id = firmwareComTaskId;

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        when(comTaskEnablement.getComTask()).thenReturn(firmwareComTask);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(comTaskEnablement)).thenReturn(comTaskExecutionBuilder);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getComTask()).thenReturn(firmwareComTask);
        when(comTaskExecution.getId()).thenReturn(firmwareComTaskExecutionId);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        when(communicationTaskService.findComTaskExecution(firmwareComTaskExecutionId)).thenReturn(Optional.of(comTaskExecution));
        return schedulingInfo;
    }

    @Test
    public void canNotCreateForFirmwareComTaskTest() throws IOException {
        DeviceSchedulesInfo schedulingInfo = mockDataForFirmwareComTaskTests();

        Response response = target("/devices/1/schedules").request().post(Entity.json(schedulingInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK.getKey());
    }

    @Test
    public void canNotUpdateForFirmwareComTaskTest() throws IOException {
        DeviceSchedulesInfo schedulingInfo = new DeviceSchedulesInfo();
        schedulingInfo.id = firmwareComTaskExecutionId;

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        when(comTaskEnablement.getComTask()).thenReturn(firmwareComTask);

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(comTaskEnablement)).thenReturn(comTaskExecutionBuilder);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getComTask()).thenReturn(firmwareComTask);
        when(comTaskExecution.getId()).thenReturn(firmwareComTaskExecutionId);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution);
        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));
        when(communicationTaskService.findComTaskExecution(firmwareComTaskExecutionId)).thenReturn(Optional.of(comTaskExecution));

        Response response = target("/devices/1/schedules").request().put(Entity.json(schedulingInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK.getKey());
    }
}
