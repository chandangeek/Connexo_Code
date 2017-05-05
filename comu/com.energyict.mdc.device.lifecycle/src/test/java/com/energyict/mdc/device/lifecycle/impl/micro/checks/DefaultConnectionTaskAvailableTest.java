/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DefaultConnectionTaskAvailable} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-14 (15:31)
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultConnectionTaskAvailableTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;

    @Test
    public void deviceWithoutConnectionTasks() {
        DefaultConnectionTaskAvailable microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
    }

    @Test
    public void deviceWithoutDefaultConnectionTask() {
        DefaultConnectionTaskAvailable microCheck = this.getTestInstance();
        ScheduledConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.isDefault()).thenReturn(false);
        ScheduledConnectionTask ct2 = mock(ScheduledConnectionTask.class);
        when(ct2.isDefault()).thenReturn(false);
        when(this.device.getConnectionTasks()).thenReturn(Arrays.asList(ct1, ct2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
    }

    @Test
    public void deviceWithOnlyDefaultConnectionTask() {
        DefaultConnectionTaskAvailable microCheck = this.getTestInstance();
        ScheduledConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.isDefault()).thenReturn(true);
        when(this.device.getConnectionTasks()).thenReturn(Collections.singletonList(ct1));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithMixOfConnectionTasks() {
        DefaultConnectionTaskAvailable microCheck = this.getTestInstance();
        ScheduledConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.isDefault()).thenReturn(true);
        ScheduledConnectionTask ct2 = mock(ScheduledConnectionTask.class);
        when(ct2.isDefault()).thenReturn(false);
        when(this.device.getConnectionTasks()).thenReturn(Arrays.asList(ct1, ct2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    private DefaultConnectionTaskAvailable getTestInstance() {
        return new DefaultConnectionTaskAvailable(this.thesaurus);
    }

}