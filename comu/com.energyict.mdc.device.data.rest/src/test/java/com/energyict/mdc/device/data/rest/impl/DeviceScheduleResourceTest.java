package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceScheduleResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void testGetComTaskExecutionsForComTaskExecutionWithoutEnablement() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        Map<String, Object> response = target("/devices/1/schedules").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("schedules");
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) response.get("schedules");
        assertThat(schedules).hasSize(0);
    }

    @Test
    public void testGetComTaskExecutionsForEnablement() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mock(ComTask.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        Map<String, Object> response = target("/devices/1/schedules").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("schedules");
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) response.get("schedules");
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0))
                .containsKey("name")
                .containsKey("id")
                .containsKey("masterScheduleId")
                .containsKey("schedule")
                .containsKey("plannedDate")
                .containsKey("nextCommunication")
                .containsKey("comTaskInfos")
                .containsKey("type");
    }

    @Test
    public void testGetComTaskExecutionsForEnablementWhichHasAdHocExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mock(ComTask.class);

        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.isAdHoc()).thenReturn(true);

        Map<String, Object> response = target("/devices/1/schedules").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("schedules");
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) response.get("schedules");
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).get("type")).isEqualTo("ADHOC");
        assertThat(schedules.get(0))
                .containsKey("name")
                .containsKey("id")
                .containsKey("masterScheduleId")
                .containsKey("schedule")
                .containsKey("plannedDate")
                .containsKey("nextCommunication")
                .containsKey("comTaskInfos")
                .containsKey("type");
    }

    @Test
    public void testGetComTaskExecutionsForEnablementWhichHasManualExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mock(ComTask.class);

        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.isScheduledManually()).thenReturn(true);
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));

        Map<String, Object> response = target("/devices/1/schedules").request().get(Map.class);
        assertThat(response).hasSize(2).containsKey("total").containsKey("schedules");
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) response.get("schedules");
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).get("type")).isEqualTo("INDIVIDUAL");
        assertThat(schedules.get(0))
                .containsKey("name")
                .containsKey("id")
                .containsKey("masterScheduleId")
                .containsKey("schedule")
                .containsKey("plannedDate")
                .containsKey("nextCommunication")
                .containsKey("comTaskInfos")
                .containsKey("type");
    }

    @Test
    public void testGetComTaskExecutionsForEnablementWhichHasScheduledExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ScheduledComTaskExecution comTaskExecution = mock(ScheduledComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mock(ComTask.class);

        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.usesSharedSchedule()).thenReturn(true);
        when(comTaskExecution.getComSchedule()).thenReturn(mock(ComSchedule.class));
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));

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
                .containsKey("comTaskInfos")
                .containsKey("type");
    }

    @Test
    public void testCreateScheduledComTaskExecutionFromEnablement() throws Exception {
        SchedulingInfo schedulingInfo = new SchedulingInfo();
        schedulingInfo.id = 111;
        schedulingInfo.schedule = TemporalExpressionInfo.from(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ComTask comTask = mock(ComTask.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTask.getId()).thenReturn(111L);


        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, schedulingInfo.schedule.asTemporalExpression())).thenReturn(comTaskExecutionBuilder);

        Response response = target("/devices/1/schedules").request().post(Entity.json(schedulingInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(comTaskExecutionBuilder, times(1)).add();
        verify(device, times(1)).newManuallyScheduledComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties, schedulingInfo.schedule.asTemporalExpression());
    }

    @Test
    public void testChangeScheduleOnManuallyScheduledComTaskExecution() throws Exception {
        SchedulingInfo schedulingInfo = new SchedulingInfo();
        schedulingInfo.id = 111;
        schedulingInfo.schedule = TemporalExpressionInfo.from(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mock(ComTask.class);
        when(comTaskExecution.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.isScheduledManually()).thenReturn(true);
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));

        ManuallyScheduledComTaskExecutionUpdater scheduledComTaskExecutionUpdater = mock(ManuallyScheduledComTaskExecutionUpdater.class);
        when(comTaskExecution.getUpdater()).thenReturn(scheduledComTaskExecutionUpdater);
        when(scheduledComTaskExecutionUpdater.scheduleAccordingTo(schedulingInfo.schedule.asTemporalExpression())).thenReturn(scheduledComTaskExecutionUpdater);

        Response response = target("/devices/1/schedules").request().put(Entity.json(schedulingInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(scheduledComTaskExecutionUpdater, times(1)).scheduleAccordingTo(schedulingInfo.schedule.asTemporalExpression());
        verify(scheduledComTaskExecutionUpdater, times(1)).update();
    }

    @Test
    public void testRemoveScheduleOnManuallyScheduledComTaskExecution() throws Exception {
        SchedulingInfo schedulingInfo = new SchedulingInfo();
        schedulingInfo.id = 111;

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);

        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        ComTask comTask = mock(ComTask.class);
        when(comTaskExecution.getId()).thenReturn(111L);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));
        when(comTaskExecution.isScheduledManually()).thenReturn(true);
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.hours(5), TimeDuration.minutes(5)));
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));


        Response response = target("/devices/1/schedules").request().put(Entity.json(schedulingInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(device, times(1)).removeComTaskExecution(comTaskExecution);
    }

    @Test
    public void testRunAdHocComTaskFromEnablement() throws Exception {
        SchedulingInfo schedulingInfo = new SchedulingInfo();
        schedulingInfo.id = 111;

        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ComTask comTask = mock(ComTask.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTask.getId()).thenReturn(111L);


        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(Optional.of(protocolDialectConfigurationProperties));
        ComTaskExecutionBuilder comTaskExecutionBuilder = mock(ComTaskExecutionBuilder.class);
        when(device.newAdHocComTaskExecution(comTaskEnablement, protocolDialectConfigurationProperties)).thenReturn(comTaskExecutionBuilder);
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution);

        Response response = target("/devices/1/schedules").request().post(Entity.json(schedulingInfo));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(comTaskExecutionBuilder, times(1)).add();
        verify(comTaskExecution, times(1)).scheduleNow();
    }

    @Test
    public void testDeleteComTaskExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZAFB001")).thenReturn(Optional.of(device));

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(12L);
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        when(comTaskExecution2.getId()).thenReturn(13L);

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution2, comTaskExecution));

        Response response = target("/devices/ZAFB001/schedules/12").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(device).removeComTaskExecution(comTaskExecution);
    }

    @Test
    public void testDeleteNonExistingComTaskExecution() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZAFB001")).thenReturn(Optional.of(device));

        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(12L);
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        when(comTaskExecution2.getId()).thenReturn(13L);

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution2, comTaskExecution));

        Response response = target("/devices/ZAFB001/schedules/666").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.message")).isEqualTo(MessageSeeds.NO_SUCH_COM_TASK_EXEC.getDefaultFormat());
        assertThat(jsonModel.<String>get("$.error")).isEqualTo(MessageSeeds.NO_SUCH_COM_TASK_EXEC.getKey());
    }
}
