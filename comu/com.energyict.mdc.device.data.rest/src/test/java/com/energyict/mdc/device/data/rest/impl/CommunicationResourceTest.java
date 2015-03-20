package com.energyict.mdc.device.data.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.tasks.ComTask;
import com.jayway.jsonpath.JsonModel;

public class CommunicationResourceTest extends DeviceDataRestApplicationJerseyTest {

    Instant startTime = Instant.now();
    Instant endTime = Instant.now();
    Instant nextCommunicationTime = Instant.now();
    Instant plannedDate = Instant.now();

    @Test
    public void testGetDeviceCommunications() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        String response = target("/devices/ZABF0000000/communications").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.communications")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.communications[0].id")).isEqualTo(13);
        assertThat(jsonModel.<String>get("$.communications[0].name")).isEqualTo("Read all");
        assertThat(jsonModel.<Integer>get("$.communications[0].comTask.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.communications[0].comTask.name")).isEqualTo("Read all");
        assertThat(jsonModel.<String>get("$.communications[0].comScheduleName")).isEqualTo("Individual");
        assertThat(jsonModel.<Integer>get("$.communications[0].comScheduleFrequency.every.count")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.communications[0].comScheduleFrequency.every.timeUnit")).isEqualTo("hours");
        assertThat(jsonModel.<Integer>get("$.communications[0].urgency")).isEqualTo(100);
        assertThat(jsonModel.<String>get("$.communications[0].currentState.id")).isEqualTo("Busy");
        assertThat(jsonModel.<String>get("$.communications[0].currentState.displayValue")).isEqualTo("Busy");
        assertThat(jsonModel.<String>get("$.communications[0].latestResult.id")).isEqualTo("IoError");
        assertThat(jsonModel.<String>get("$.communications[0].latestResult.displayValue")).isEqualTo("I/O error");
        assertThat(jsonModel.<Long>get("$.communications[0].startTime")).isEqualTo(startTime.toEpochMilli());
        assertThat(jsonModel.<Long>get("$.communications[0].successfulFinishTime")).isEqualTo(endTime.toEpochMilli());
        assertThat(jsonModel.<Long>get("$.communications[0].nextCommunication")).isEqualTo(nextCommunicationTime.toEpochMilli());
        assertThat(jsonModel.<Long>get("$.communications[0].plannedDate")).isEqualTo(plannedDate.toEpochMilli());
        assertThat(jsonModel.<String>get("$.communications[0].connectionMethod")).isEqualTo("connection task (Default)");
        assertThat(jsonModel.<String>get("$.communications[0].connectionStrategy.id")).isEqualTo("asSoonAsPossible");
        assertThat(jsonModel.<String>get("$.communications[0].connectionStrategy.displayValue")).isEqualTo("As soon as possible");
        assertThat(jsonModel.<Boolean>get("$.communications[0].isOnHold")).isTrue();
    }

    @Test
    public void testGetDeviceCommunicationsSeveralComTasksPerComTaskExec() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        List<ComTask> comTasks = Arrays.asList(mockComTask(1, "Read 1"), mockComTask(2, "Read 2"));
        when(comTaskExecution.getComTasks()).thenReturn(comTasks);
        when(comTaskExecution.getDevice()).thenReturn(device);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfig);
        ComTaskEnablement comTaskEnablement = mockComTaskEnablement(mockComTask(2L, "Read 2"));
        when(deviceConfig.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        String response = target("/devices/ZABF0000000/communications").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.communications")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.communications[0].comTask.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.communications[0].comTask.name")).isEqualTo("Read 2");
    }

    @Test
    public void testRunCommunication() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        Response response = target("/devices/ZABF0000000/communications/13/run").request().put(Entity.json(""));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution).scheduleNow();
    }

    @Test
    public void testRunNowCommunication() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        Response response = target("/devices/ZABF0000000/communications/13/runnow").request().put(Entity.json(""));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution).runNow();
    }

    @Test
    public void testActivateCommunication() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isOnHold()).thenReturn(true);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        DeviceComTaskExecutionInfo info = new DeviceComTaskExecutionInfo();
        info.isOnHold = false;

        Response response = target("/devices/ZABF0000000/communications/13").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution).updateNextExecutionTimestamp();
    }

    @Test
    public void testActivateCommunicationButAlreadyActive() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isOnHold()).thenReturn(false);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        DeviceComTaskExecutionInfo info = new DeviceComTaskExecutionInfo();
        info.isOnHold = false;

        Response response = target("/devices/ZABF0000000/communications/13").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution, VerificationModeFactory.times(0)).updateNextExecutionTimestamp();
    }

    @Test
    public void testDeactivateCommunication() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isOnHold()).thenReturn(false);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        DeviceComTaskExecutionInfo info = new DeviceComTaskExecutionInfo();
        info.isOnHold = true;

        Response response = target("/devices/ZABF0000000/communications/13").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution).putOnHold();
    }

    @Test
    public void testDeactivateCommunicationButAlreadyInactive() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isOnHold()).thenReturn(true);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));
        DeviceComTaskExecutionInfo info = new DeviceComTaskExecutionInfo();
        info.isOnHold = true;

        Response response = target("/devices/ZABF0000000/communications/13").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution, VerificationModeFactory.times(0)).putOnHold();
    }

    @Test
    public void testActivateAllCommunications() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isOnHold()).thenReturn(true);
        ComTaskExecution comTaskExecution1 = mockComTaskExecution();
        when(comTaskExecution1.isOnHold()).thenReturn(true);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution, comTaskExecution1));

        Response response = target("/devices/ZABF0000000/communications/activate").request().put(Entity.json(""));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution).updateNextExecutionTimestamp();
        verify(comTaskExecution1).updateNextExecutionTimestamp();
    }

    @Test
    public void testDeactivateAllCommunications() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isOnHold()).thenReturn(false);
        ComTaskExecution comTaskExecution1 = mockComTaskExecution();
        when(comTaskExecution1.isOnHold()).thenReturn(false);
        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution, comTaskExecution1));

        Response response = target("/devices/ZABF0000000/communications/deactivate").request().put(Entity.json(""));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(comTaskExecution).putOnHold();
        verify(comTaskExecution1).putOnHold();
    }

    @Test
    public void testComTaskExecNotFound() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));

        Response response = target("/devices/ZABF0000000/communications/145").request().put(Entity.json(""));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    private ComTaskExecution mockComTaskExecution() {
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(13L);
        when(comTaskExecution.isOnHold()).thenReturn(true);
        when(comTaskExecution.getPlannedNextExecutionTimestamp()).thenReturn(plannedDate);
        ComTaskExecutionSession comTaskExecutionSession = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.IOError);
        when(comTaskExecution.getLastSession()).thenReturn(Optional.of(comTaskExecutionSession));
        ConnectionTask<?, ?> connectionTask = mockConnectionTask();
        doReturn(connectionTask).when(comTaskExecution).getConnectionTask();
        List<ComTask> comTasks = Arrays.asList(mockComTask(1L, "Read all"));
        when(comTaskExecution.getComTasks()).thenReturn(comTasks);
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.hours(1)));
        when(comTaskExecution.getExecutionPriority()).thenReturn(100);
        when(comTaskExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(comTaskExecution.getLastExecutionStartTimestamp()).thenReturn(startTime);
        when(comTaskExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(endTime);
        when(comTaskExecution.getNextExecutionTimestamp()).thenReturn(nextCommunicationTime);
        return comTaskExecution;
    }

    private ConnectionTask<?, ?> mockConnectionTask() {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(partialConnectionTask.getName()).thenReturn("connection task");
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        return connectionTask;
    }

    private ComTask mockComTask(long id, String name) {
        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(id);
        when(comTask.getName()).thenReturn(name);
        return comTask;
    }

    private ComTaskEnablement mockComTaskEnablement(ComTask comTask) {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        return comTaskEnablement;
    }

}
