/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;

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
 * Tests the {@link ConnectionPropertiesAreValid} component
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionPropertiesAreValidTest {

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private Device device;
    @Mock
    private State state;

    @Test
    public void deviceWithoutConnectionTasks() {
        ConnectionPropertiesAreValid microCheck = this.getTestInstance();
        when(this.device.getConnectionTasks()).thenReturn(Collections.emptyList());

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

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
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

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
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

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
        when(cte1.getConnectionTask()).thenReturn(Optional.of(ct1));

        ConnectionTask ct2 = mock(ScheduledConnectionTask.class);
        when(ct2.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte1.getConnectionTask()).thenReturn(Optional.of(ct2));
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void deviceWithMixOfConnectionTasks() {
        ConnectionPropertiesAreValid microCheck = this.getTestInstance();
        ConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.getConnectionTask()).thenReturn(Optional.of(ct1));
        ConnectionTask ct2 = mock(ScheduledConnectionTask.class);
        when(ct2.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte2.getConnectionTask()).thenReturn(Optional.of(ct2));
        ConnectionTask ct3 = mock(ScheduledConnectionTask.class);
        when(ct3.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        ComTaskExecution cte3 = mock(ComTaskExecution.class);
        when(cte3.getConnectionTask()).thenReturn(Optional.of(ct2));
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2, cte3));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void connectionTaskWithKeyAccessorType() throws Exception {
        ConnectionPropertiesAreValid microCheck = this.getTestInstance();
        ConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        ConnectionTaskProperty prop = mock(ConnectionTaskProperty.class);
        when(prop.getConnectionTask()).thenReturn(ct1);
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(prop.getValue()).thenReturn(securityAccessorType);
        when(ct1.getProperties()).thenReturn(Collections.singletonList(prop));
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.getConnectionTask()).thenReturn(Optional.of(ct1));
        when(this.device.getComTaskExecutions()).thenReturn(Collections.singletonList(cte1));
        SecurityAccessor securityAccessor = mock(SecurityAccessor.class);
        when(securityAccessor.getActualValue()).thenReturn(Optional.of("someValue"));
        when(device.getSecurityAccessor(securityAccessorType)).thenReturn(Optional.of(securityAccessor));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void connectionTaskWithKeyAccessorTypeButNoValueOnDevice() throws Exception {
        ConnectionPropertiesAreValid microCheck = this.getTestInstance();
        ConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        ConnectionTaskProperty prop = mock(ConnectionTaskProperty.class);
        when(prop.getConnectionTask()).thenReturn(ct1);
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(prop.getValue()).thenReturn(securityAccessorType);
        when(ct1.getProperties()).thenReturn(Collections.singletonList(prop));
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.getConnectionTask()).thenReturn(Optional.of(ct1));
        when(this.device.getComTaskExecutions()).thenReturn(Collections.singletonList(cte1));
        when(device.getSecurityAccessor(securityAccessorType)).thenReturn(Optional.empty());

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void connectionTaskWithKeyAccessorTypeButNoActualValue() throws Exception {
        ConnectionPropertiesAreValid microCheck = this.getTestInstance();
        ConnectionTask ct1 = mock(ScheduledConnectionTask.class);
        ConnectionTaskProperty prop = mock(ConnectionTaskProperty.class);
        when(prop.getConnectionTask()).thenReturn(ct1);
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(prop.getValue()).thenReturn(securityAccessorType);
        when(ct1.getProperties()).thenReturn(Collections.singletonList(prop));
        when(ct1.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.getConnectionTask()).thenReturn(Optional.of(ct1));
        when(this.device.getComTaskExecutions()).thenReturn(Collections.singletonList(cte1));
        SecurityAccessor securityAccessor = mock(SecurityAccessor.class);
        when(securityAccessor.getActualValue()).thenReturn(Optional.empty());
        when(device.getSecurityAccessor(securityAccessorType)).thenReturn(Optional.of(securityAccessor));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    private ConnectionPropertiesAreValid getTestInstance() {
        ConnectionPropertiesAreValid connectionPropertiesAreValid = new ConnectionPropertiesAreValid();
        connectionPropertiesAreValid.setThesaurus(this.thesaurus);
        return connectionPropertiesAreValid;
    }
}
