/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.tasks.ComTask;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link StartCommunication} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-13 (14:34)
 */
@RunWith(MockitoJUnitRunner.class)
public class StartCommunicationTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private DeviceConfiguration deviceConfiguration;

    @Mock
    private ComTaskEnablement comTaskEnablement1, comTaskEnablement2, comTaskEnablement3;
    @Mock
    ComTaskExecutionBuilder comTaskExecutionBuilder;
    @Mock
    private ComTask comTask1, comTask2, comTask3;
    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void testGetPropertySpecs() {
        StartCommunication microAction = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = microAction.getPropertySpecs(this.propertySpecService);

        // Asserts
        assertThat(propertySpecs).isEmpty();
    }

    @Test
    public void executeActivatesAllConnectionTasks() {
        ConnectionTask connectionTask1 = mock(ConnectionTask.class);
        ConnectionTask connectionTask2 = mock(ConnectionTask.class);
        when(this.device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask1, connectionTask2));
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);

        StartCommunication microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Instant.now(), Collections.emptyList());

        // Asserts
        verify(connectionTask1).activate();
        verify(connectionTask2).activate();
    }

    @Test
    public void executeSchedulesAllCommunicationTasks() {
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        ComTaskExecution comTaskExecution3= mock(ComTaskExecution.class);
        when(this.device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1, comTaskEnablement2, comTaskEnablement3 ));
        when(device.newAdHocComTaskExecution(any(ComTaskEnablement.class))).thenReturn(comTaskExecutionBuilder);
        when(comTaskExecutionBuilder.scheduleNow()).thenReturn(comTaskExecutionBuilder);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution3);
        when(comTaskExecution1.getComTask()).thenReturn(comTask1);
        when(comTaskExecution2.getComTask()).thenReturn(comTask2);
        when(comTaskEnablement1.getComTask()).thenReturn(comTask1);
        when(comTaskEnablement2.getComTask()).thenReturn(comTask2);
        when(comTaskEnablement3.getComTask()).thenReturn(comTask3);
        when(comTask1.getId()).thenReturn(1L);
        when(comTask2.getId()).thenReturn(2L);
        when(comTask3.getId()).thenReturn(3L);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1, comTaskExecution2));
        StartCommunication microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Instant.now(), Collections.emptyList());

        // Asserts
        verify(comTaskExecutionBuilder, timeout(3)).scheduleNow();
    }

    private StartCommunication getTestInstance() {
        return new StartCommunication(thesaurus);
    }

}