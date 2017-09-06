/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.tasks.ComTask;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CommunicationResourceTest extends DeviceDataRestApplicationJerseyTest {

    Instant startTime = Instant.now();
    Instant endTime = Instant.now();
    Instant nextCommunicationTime = Instant.now();
    Instant plannedDate = Instant.now();

    ConnectionFunction connectionFunction1, connectionFunction2;

    @Before
    public void prepareConnectionFunctions() throws Exception {
        connectionFunction1 = mockConnectionFunction(1, "CF_1", "CF 1");
        connectionFunction2 = mockConnectionFunction(2, "CF_2", "CF 2");
    }

    @Test
    public void testAdditionalComTaskFields() {
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getName()).thenReturn("No security");
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(comTaskEnablement.getConnectionFunction()).thenReturn(Optional.empty());
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));

        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mockComTask(1L, "Read");
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getConnectionFunction()).thenReturn(Optional.empty());

        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));

        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getName()).thenReturn("connectionMethod");
        when(device.getConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));

        JsonModel jsonModel = JsonModel.model(target("/devices/name/comtasks/").request().get(String.class));
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.comTasks[0].isOnHold")).isTrue();
        assertThat(jsonModel.<Instant>get("$.comTasks[0].successfulFinishTime")).isNotNull();
        assertThat(jsonModel.<String>get("$.comTasks[0].latestResult.id")).isEqualTo("IOError");
    }

    @Test
    public void testAdditionalComTaskFieldsWhenSupportingConnectionFunctions() {
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getName()).thenReturn("No security");
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(comTaskEnablement.getConnectionFunction()).thenReturn(Optional.of(connectionFunction2));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));

        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mockComTask(1L, "Read");
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getConnectionFunction()).thenReturn(Optional.of(connectionFunction2));
        when(comTaskExecution.getDevice()).thenReturn(device);
        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));

        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getName()).thenReturn("connectionMethod");
        when(device.getConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));

        when(topologyService.findConnectionTaskWithConnectionFunctionForTopology(device, connectionFunction2)).thenReturn(Optional.of(connectionTask));

        JsonModel jsonModel = JsonModel.model(target("/devices/name/comtasks/").request().get(String.class));
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.comTasks[0].isOnHold")).isTrue();
        assertThat(jsonModel.<Instant>get("$.comTasks[0].successfulFinishTime")).isNotNull();
        assertThat(jsonModel.<String>get("$.comTasks[0].latestResult.id")).isEqualTo("IOError");
        assertThat(jsonModel.<Boolean>get("$.comTasks[0].connectionDefinedOnDevice")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.comTasks[0].connectionFunctionInfo.id")).isEqualTo(2);
    }

    @Test
    public void testActivateComTask() {
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("name")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("name", 1L)).thenReturn(Optional.of(device));

        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(1L, 1L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findDeviceConfiguration(1L)).thenReturn(Optional.of(deviceConfiguration));

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getName()).thenReturn("No security");
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));

        ComTaskExecution comTaskExecution = mockComTaskExecution();
        when(comTaskExecution.isScheduledManually()).thenReturn(true);

        ComTask comTask = mockComTask(1L, "Read");
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getConnectionFunction()).thenReturn(Optional.of(connectionFunction2));
        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));

        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getName()).thenReturn("connectionMethod");
        when(device.getConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));

        ComTaskConnectionMethodInfo info = new ComTaskConnectionMethodInfo();
        info.device = new DeviceInfo();
        info.device.mRID = "mrid";
        info.device.version = 1L;
        info.device.parent = new VersionInfo<>(1L, 1L);
        Response response = target("/devices/name/comtasks/1/activate").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    private ComTaskExecution mockComTaskExecution() {
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(13L);
        when(comTaskExecution.isOnHold()).thenReturn(true);
        when(comTaskExecution.getPlannedNextExecutionTimestamp()).thenReturn(plannedDate);
        ComTaskExecutionSession comTaskExecutionSession = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.IOError);
        when(comTaskExecution.getLastSession()).thenReturn(Optional.of(comTaskExecutionSession));
        ConnectionTask<?, ?> connectionTask = mockConnectionTask();
        doReturn(Optional.of(connectionTask)).when(comTaskExecution).getConnectionTask();
        ComTask comTask = mockComTask(1L, "Read all");
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        NextExecutionSpecs nextExecutionSpecs = mock(NextExecutionSpecs.class);
        when(comTaskExecution.getNextExecutionSpecs()).thenReturn(Optional.of(nextExecutionSpecs));
        when(nextExecutionSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.hours(1)));
        when(comTaskExecution.getExecutionPriority()).thenReturn(100);
        when(comTaskExecution.getStatus()).thenReturn(TaskStatus.Busy);
        when(comTaskExecution.getLastExecutionStartTimestamp()).thenReturn(startTime);
        when(comTaskExecution.getLastSuccessfulCompletionTimestamp()).thenReturn(endTime);
        when(comTaskExecution.getNextExecutionTimestamp()).thenReturn(nextCommunicationTime);
        DeviceProtocolDialect deviceProtocolDialect = mock(DeviceProtocolDialect.class);
        when(deviceProtocolDialect.getDeviceProtocolDialectDisplayName()).thenReturn("WebRTU KP");
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

    private ConnectionFunction mockConnectionFunction(int id, String name, String displayName) {
        return new ConnectionFunction() {
            @Override
            public long getId() {
                return id;
            }

            @Override
            public String getConnectionFunctionName() {
                return name;
            }

            @Override
            public String getConnectionFunctionDisplayName() {
                return displayName;
            }
        };
    }
}
