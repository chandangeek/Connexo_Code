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
 * Tests the {@link StartCommunication} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-13 (14:34)
 */
@RunWith(MockitoJUnitRunner.class)
public class StartRecurringCommunicationTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;

    @Test
    public void testGetPropertySpecs() {
        StartRecurringCommunication microAction = this.getTestInstance();

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
        StartRecurringCommunication microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verify(connectionTask1).activate();
        verify(connectionTask2).activate();
    }

    @Test
    public void executeSchedulesAllCommunicationTasks() {
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1, comTaskExecution2));
        StartRecurringCommunication microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verify(comTaskExecution1).scheduleNow();
        verify(comTaskExecution2).scheduleNow();
    }

    private StartRecurringCommunication getTestInstance() {
        return new StartRecurringCommunication();
    }

}