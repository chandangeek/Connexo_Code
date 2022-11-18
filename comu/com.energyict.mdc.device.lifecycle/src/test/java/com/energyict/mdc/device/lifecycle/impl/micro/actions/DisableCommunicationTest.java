/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DisableCommunication} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-06 (08:48)
 */
@RunWith(MockitoJUnitRunner.class)
public class DisableCommunicationTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)

    private PropertySpecService propertySpecService;
    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceService deviceService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private ConnectionTaskService connectionTaskService;

    @Test
    public void testGetPropertySpecs() {
        DisableCommunication microAction = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = microAction.getPropertySpecs(this.propertySpecService);

        // Asserts
        assertThat(propertySpecs).isEmpty();
    }

    @Test
    public void executeDeactivatesAllConnectionTasks() {
        ConnectionTask connectionTask1 = mock(ConnectionTask.class);
        ConnectionTask connectionTask2 = mock(ConnectionTask.class);
        when(connectionTask1.getId()).thenReturn(1L);
        when(connectionTask2.getId()).thenReturn(2L);
        when(connectionTaskService.findAndLockConnectionTaskById(1)).thenReturn(Optional.of(connectionTask1));
        when(connectionTaskService.findAndLockConnectionTaskById(2)).thenReturn(Optional.of(connectionTask2));
        when(this.device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask1, connectionTask2));
        when(this.device.getDeviceConfiguration()).thenReturn(mock(DeviceConfiguration.class));
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.of(device));
        DisableCommunication microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Instant.now(), Collections.emptyList());

        // Asserts
        verify(connectionTask1).deactivate();
        verify(connectionTask2).deactivate();
    }

    @Test
    public void executePutsAllCommunicationTasksOnHold() {
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        when(comTaskExecution1.getId()).thenReturn(1L);
        when(comTaskExecution2.getId()).thenReturn(2L);
        ComTask comTask1 = mock(ComTask.class);
        ComTask comTask2 = mock(ComTask.class);
        when(comTask1.getId()).thenReturn(1L);
        when(comTask2.getId()).thenReturn(2L);
        when(comTaskExecution1.getComTask()).thenReturn(comTask1);
        when(comTaskExecution2.getComTask()).thenReturn(comTask2);
        when(communicationTaskService.findAndLockComTaskExecutionById(1)).thenReturn(Optional.of(comTaskExecution1));
        when(communicationTaskService.findAndLockComTaskExecutionById(2)).thenReturn(Optional.of(comTaskExecution2));
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1, comTaskExecution2));
        when(this.device.getDeviceConfiguration()).thenReturn(mock(DeviceConfiguration.class));
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.of(device));
        DisableCommunication microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Instant.now(), Collections.emptyList());

        // Asserts
        verify(comTaskExecution1).putOnHold();
        verify(comTaskExecution2).putOnHold();
    }

    private DisableCommunication getTestInstance() {
        return new DisableCommunication(thesaurus, deviceService, this.communicationTaskService, this.connectionTaskService);
    }

}
