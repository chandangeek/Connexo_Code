package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.jayway.jsonpath.JsonModel;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 10/3/14.
 */
public class ComTaskExecutionSessionResourceTest extends DeviceDataRestApplicationJerseyTest {
    private final Date start = new Date(1412341200000L);
    private final Date end = new Date(1412341300000L);

    @Test
    public void testGetComTaskExecutionsWithScheduleComTask() throws Exception {
        Device device = mock(Device.class);
        ConnectionTask<?, ?> connectionTask = mock(ConnectionTask.class);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(device.getmRID()).thenReturn("0c53c750-4d5a-11e4-916c-0800200c9a66");
        when(device.getName()).thenReturn("AX1");
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(123L);
        when(deviceConfiguration.getName()).thenReturn("config AX1");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(125L);
        when(deviceType.getName()).thenReturn("type AX1");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceService.findByUniqueMrid("0c53c750-4d5a-11e4-916c-0800200c9a66")).thenReturn(device);
        when(connectionTask.getId()).thenReturn(3L);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getName()).thenReturn("GPRS");
        ComSession comSession1 = mockComSession(connectionTask, 61L, device);
        when(connectionTaskService.findAllSessionsFor(connectionTask)).thenReturn(Arrays.asList(comSession1));
        String response = target("/devices/0c53c750-4d5a-11e4-916c-0800200c9a66/connectionmethods/3/comsessions/61/comtaskexecutionsessions").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<String>get("$.device")).isEqualTo("AX1");
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.comTaskExecutionSessions")).hasSize(1);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].name")).isEqualTo("Read all + Basic check + Set clock");
        assertThat(jsonModel.<List>get("$.comTaskExecutionSessions[0].comTasks")).hasSize(2);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comTasks[0].name")).isEqualTo("Read all + Basic check");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comTasks[0].id")).isEqualTo(1001);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comTasks[1].name")).isEqualTo("Set clock");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comTasks[1].id")).isEqualTo(1002);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].device.id")).isEqualTo("0c53c750-4d5a-11e4-916c-0800200c9a66");
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].device.name")).isEqualTo("AX1");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].deviceConfiguration.id")).isEqualTo(123);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].deviceConfiguration.name")).isEqualTo("config AX1");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].deviceType.id")).isEqualTo(125);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].deviceType.name")).isEqualTo("type AX1");
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comScheduleName")).isEqualTo("als ge eens tijd hebt");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comScheduleFrequency.every.count")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comScheduleFrequency.every.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].comScheduleFrequency.offset.count")).isEqualTo(10);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].comScheduleFrequency.offset.timeUnit")).isEqualTo("seconds");
        assertThat(jsonModel.<Boolean>get("$.comTaskExecutionSessions[0].comScheduleFrequency.lastDay")).isEqualTo(false);
        assertThat(jsonModel.<Integer>get("$.comTaskExecutionSessions[0].urgency")).isEqualTo(-20);
        assertThat(jsonModel.<String>get("$.comTaskExecutionSessions[0].result")).isEqualTo("Failure");
        assertThat(jsonModel.<Long>get("$.comTaskExecutionSessions[0].startTime")).isEqualTo(1412341200000L);
        assertThat(jsonModel.<Long>get("$.comTaskExecutionSessions[0].finishTime")).isEqualTo(1412341300000L);
        assertThat(jsonModel.<Boolean>get("$.comTaskExecutionSessions[0].alwaysExecuteOnInbound")).isEqualTo(true);
    }

    private ComSession mockComSession(ConnectionTask<?, ?> connectionTask, Long id, Device device) {
        ComSession comSession = mock(ComSession.class);
        when(comSession.getId()).thenReturn(id);
        when(comSession.getStartDate()).thenReturn(start.toInstant());
        when(comSession.getStopDate()).thenReturn(end.toInstant());
        ComPort comPort = mock(ComPort.class);
        when(comPort.getName()).thenReturn("comPort 199812981212");
        when(comPort.getId()).thenReturn(199812981212L);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(1234654L);
        when(comServer.getName()).thenReturn("communication server alfa");
        when(comPort.getComServer()).thenReturn(comServer);
        when(comSession.getComPort()).thenReturn(comPort);
        when(comSession.getConnectionTask()).thenReturn(connectionTask);
        when(comSession.getNumberOfSuccessFulTasks()).thenReturn(1001);
        when(comSession.getNumberOfFailedTasks()).thenReturn(1002);
        when(comSession.getNumberOfPlannedButNotExecutedTasks()).thenReturn(1003);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Success);
        ComTaskExecutionSession comTaskExecutionSession = mockComTaskExecutionSession(device, comSession);
        when(comSession.getComTaskExecutionSessions()).thenReturn(Arrays.asList(comTaskExecutionSession));
        return comSession;
    }

    private ComTaskExecutionSession mockComTaskExecutionSession(Device device, ComSession comSession) {
        ComTaskExecutionSession comTaskExecutionSession = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession.getComSession()).thenReturn(comSession);
        when(comTaskExecutionSession.getDevice()).thenReturn(device);
        ScheduledComTaskExecution comTaskExecution = mock(ScheduledComTaskExecution.class);
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("15 minutes"), new TimeDuration("10 seconds")));
        when(comSchedule.getName()).thenReturn("als ge eens tijd hebt");
        when(comTaskExecution.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution.isIgnoreNextExecutionSpecsForInbound()).thenReturn(true);
        when(comTaskExecution.getExecutionPriority()).thenReturn(-20);
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getId()).thenReturn(1001L);
        when(comTask1.getName()).thenReturn("Read all + Basic check");
        ComTask comTask2 = mock(ComTask.class);
        when(comTask2.getId()).thenReturn(1002L);
        when(comTask2.getName()).thenReturn("Set clock");
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask2, comTask1));
        when(comTaskExecutionSession.getComTaskExecution()).thenReturn(comTaskExecution);
        when(comTaskExecutionSession.getSuccessIndicator()).thenReturn(ComTaskExecutionSession.SuccessIndicator.Failure);
        when(comTaskExecutionSession.getStartDate()).thenReturn(start);
        when(comTaskExecutionSession.getStopDate()).thenReturn(end);
        return comTaskExecutionSession;
    }
}
