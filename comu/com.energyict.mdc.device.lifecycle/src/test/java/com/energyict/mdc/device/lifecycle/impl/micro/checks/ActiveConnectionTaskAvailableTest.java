package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ActiveConnectionAvailable} component.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ActiveConnectionTaskAvailableTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;

    @Test
    public void deviceWithoutConnectionTasks() {
        ActiveConnectionAvailable microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE);
    }

    @Test
    public void deviceWithoutActiveConnectionTask() {
        ActiveConnectionAvailable microCheck = this.getTestInstance();
        ScheduledConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        ScheduledConnectionTask ct2 = mock(ScheduledConnectionTask.class);
        when(ct2.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        when(this.device.getConnectionTasks()).thenReturn(Arrays.asList(ct1, ct2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE);
    }

    @Test
    public void deviceWithOnly1ActiveConnectionTask() {
        ActiveConnectionAvailable microCheck = this.getTestInstance();
        ScheduledConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        when(this.device.getConnectionTasks()).thenReturn(Collections.singletonList(ct1));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithMixOfConnectionTasks() {
        ActiveConnectionAvailable microCheck = this.getTestInstance();
        ScheduledConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        ScheduledConnectionTask ct2 = mock(ScheduledConnectionTask.class);
        when(ct2.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        when(this.device.getConnectionTasks()).thenReturn(Arrays.asList(ct1, ct2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    private ActiveConnectionAvailable getTestInstance() {
        return new ActiveConnectionAvailable(this.thesaurus);
    }

}