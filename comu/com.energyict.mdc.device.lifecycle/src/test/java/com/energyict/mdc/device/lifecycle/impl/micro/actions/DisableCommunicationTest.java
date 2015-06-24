package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(this.device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask1, connectionTask2));
        DisableCommunication microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verify(connectionTask1).deactivate();
        verify(connectionTask2).deactivate();
    }

    @Test
    public void executePutsAllCommunicationTasksOnHold() {
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1, comTaskExecution2));
        DisableCommunication microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verify(comTaskExecution1).putOnHold();
        verify(comTaskExecution2).putOnHold();
    }

    private DisableCommunication getTestInstance() {
        return new DisableCommunication();
    }

}