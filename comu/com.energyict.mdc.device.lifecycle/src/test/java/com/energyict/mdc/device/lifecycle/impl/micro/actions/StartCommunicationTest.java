/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.ConnectionTask;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
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
        comTaskExecution1.putOnHold();
        comTaskExecution2.putOnHold();
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1, comTaskExecution2));
        when(this.device.getDeviceConfiguration()).thenReturn(mock(DeviceConfiguration.class));
        StartCommunication microAction = this.getTestInstance();
        microAction.execute(this.device, Instant.now(), Collections.emptyList());


        Assert.assertFalse(comTaskExecution1.isOnHold());
        Assert.assertFalse(comTaskExecution2.isOnHold());
    }

    private StartCommunication getTestInstance() {
        return new StartCommunication(thesaurus);
    }

}