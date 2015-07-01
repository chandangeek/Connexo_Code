package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ConnectionPropertiesAreValid} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-16 (16:52)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionPropertiesAreValidTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;

    @Test
    public void deviceWithoutConnectionTasks() {
        ConnectionPropertiesAreValid microCheck = this.getTestInstance();
        when(this.device.getConnectionTasks()).thenReturn(Collections.emptyList());

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithOnlyActiveConnectionTasks() {
        ConnectionPropertiesAreValid microCheck = this.getTestInstance();
        ScheduledConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        ScheduledConnectionTask ct2 = mock(ScheduledConnectionTask.class);
        when(ct2.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        when(this.device.getConnectionTasks()).thenReturn(Arrays.asList(ct1, ct2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithOnlyInactiveConnectionTasks() {
        ConnectionPropertiesAreValid microCheck = this.getTestInstance();
        ScheduledConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        ScheduledConnectionTask ct2 = mock(ScheduledConnectionTask.class);
        when(ct2.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        when(this.device.getConnectionTasks()).thenReturn(Arrays.asList(ct1, ct2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void deviceWithOnlyIncompleteConnectionTasks() {
        ConnectionPropertiesAreValid microCheck = this.getTestInstance();
        ConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.getConnectionTask()).thenReturn(ct1);

        ConnectionTask ct2 = mock(ScheduledConnectionTask.class);
        when(ct2.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte1.getConnectionTask()).thenReturn(ct2);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void deviceWithMixOfConnectionTasks() {
        ConnectionPropertiesAreValid microCheck = this.getTestInstance();
        ConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.getConnectionTask()).thenReturn(ct1);
        ConnectionTask ct2 = mock(ScheduledConnectionTask.class);
        when(ct2.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte2.getConnectionTask()).thenReturn(ct2);
        ConnectionTask ct3 = mock(ScheduledConnectionTask.class);
        when(ct3.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        ComTaskExecution cte3 = mock(ComTaskExecution.class);
        when(cte3.getConnectionTask()).thenReturn(ct2);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2, cte3));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
    }

    private ConnectionPropertiesAreValid getTestInstance() {
        return new ConnectionPropertiesAreValid(this.thesaurus);
    }

}