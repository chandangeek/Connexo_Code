package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.tasks.ComTask;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CommunicationResourceTest extends DeviceDataRestApplicationJerseyTest {

    Instant startTime = Instant.now();
    Instant endTime = Instant.now();
    Instant nextCommunicationTime = Instant.now();
    Instant plannedDate = Instant.now();

    @Test
    public void testAdditionalComTaskFields() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getName()).thenReturn("No security");
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mockComTask(1L, "Read");
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getName()).thenReturn("connectionMethod");
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));

        JsonModel jsonModel = JsonModel.model(target("/devices/mrid/comtasks/").request().get(String.class));
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.comTasks[0].isOnHold")).isTrue();
        assertThat(jsonModel.<Instant>get("$.comTasks[0].successfulFinishTime")).isNotNull();
        assertThat(jsonModel.<String>get("$.comTasks[0].latestResult.id")).isEqualTo("IoError");
    }

    @Test
    public void testActivateComTask() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("mrid")).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getName()).thenReturn("No security");
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));

        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mockComTask(1L, "Read");
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        when(device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution));

        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getName()).thenReturn("connectionMethod");
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));

        Response response = target("/devices/mrid/comtasks/1/activate").request().put(Entity.json(""));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
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
        doReturn(Optional.of(connectionTask)).when(comTaskExecution).getConnectionTask();
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
        DeviceProtocolDialect deviceProtocolDialect = mock(DeviceProtocolDialect.class);
        when(deviceProtocolDialect.getDisplayName()).thenReturn("WebRTU KP");
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getDeviceProtocolDialect()).thenReturn(deviceProtocolDialect);
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
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
        when(taskService.findComTask(id)).thenReturn(Optional.of(comTask));
        return comTask;
    }

    private ComTaskEnablement mockComTaskEnablement(ComTask comTask) {
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        return comTaskEnablement;
    }

}
