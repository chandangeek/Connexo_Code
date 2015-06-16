package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.*;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.energyict.mdc.tasks.ComTask;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
public class StartCommunicationTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private DeviceConfiguration deviceConfiguration;

    @Mock
    private ComTaskEnablement comTaskEnablement1, comTaskEnablement2, comTaskEnablement3;
    @Mock
    ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder;
    @Mock
    private ComTask comTask1, comTask2, comTask3;
    @Mock
    private Device device;

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
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verify(connectionTask1).activate();
        verify(connectionTask2).activate();
    }

    @Test
    public void executeSchedulesAllCommunicationTasks() {
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        ManuallyScheduledComTaskExecution comTaskExecution3= mock(ManuallyScheduledComTaskExecution.class);
        when(this.device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1, comTaskEnablement2, comTaskEnablement3 ));
        when(device.newManuallyScheduledComTaskExecution(any(ComTaskEnablement.class), any(TemporalExpression.class))).thenReturn(comTaskExecutionBuilder);
        when(comTaskExecutionBuilder.add()).thenReturn(comTaskExecution3);
        when(comTaskExecution1.getComTasks()).thenReturn(Collections.singletonList(comTask1));
        when(comTaskExecution2.getComTasks()).thenReturn(Collections.singletonList(comTask2));
        when(comTaskEnablement1.getComTask()).thenReturn(comTask1);
        when(comTaskEnablement2.getComTask()).thenReturn(comTask2);
        when(comTaskEnablement3.getComTask()).thenReturn(comTask3);
        when(comTask1.getId()).thenReturn(1L);
        when(comTask2.getId()).thenReturn(2L);
        when(comTask3.getId()).thenReturn(3L);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(comTaskExecution1, comTaskExecution2, comTaskExecution3));
        StartCommunication microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Collections.emptyList());

        // Asserts
        verify(comTaskExecution1).scheduleNow();
        verify(comTaskExecution2).scheduleNow();
        verify(comTaskExecution3).scheduleNow();
    }

    private StartCommunication getTestInstance() {
        return new StartCommunication();
    }

}