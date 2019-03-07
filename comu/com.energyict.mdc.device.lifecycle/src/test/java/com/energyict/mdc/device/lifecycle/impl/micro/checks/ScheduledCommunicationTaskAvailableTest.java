/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
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
 * Tests the {@link ScheduledCommunicationTaskAvailable} component
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduledCommunicationTaskAvailableTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;

    @Test
    public void deviceWithoutCommunicationTasks() {
        ScheduledCommunicationTaskAvailable microCheck = this.getTestInstance();
        when(this.device.getComTaskExecutions()).thenReturn(Collections.emptyList());

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void deviceWithOnlyAdHocCommunicationTasks() {
        ScheduledCommunicationTaskAvailable microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.isScheduledManually()).thenReturn(true);
        when(cte1.isAdHoc()).thenReturn(true);
        when(cte1.usesSharedSchedule()).thenReturn(false);
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte2.isScheduledManually()).thenReturn(true);
        when(cte2.isAdHoc()).thenReturn(true);
        when(cte2.usesSharedSchedule()).thenReturn(false);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void deviceWithOneManuallyScheduledCommunicationTask() {
        ScheduledCommunicationTaskAvailable microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.isScheduledManually()).thenReturn(true);
        when(cte1.isAdHoc()).thenReturn(false);
        when(cte1.usesSharedSchedule()).thenReturn(false);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithOneSharedScheduledCommunicationTask() {
        ScheduledCommunicationTaskAvailable microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.isScheduledManually()).thenReturn(false);
        when(cte1.isAdHoc()).thenReturn(false);
        when(cte1.usesSharedSchedule()).thenReturn(true);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithMixOfCommunicationTasks() {
        ScheduledCommunicationTaskAvailable microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.isScheduledManually()).thenReturn(true);
        when(cte1.isAdHoc()).thenReturn(true);
        when(cte1.usesSharedSchedule()).thenReturn(false);
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte2.isScheduledManually()).thenReturn(true);
        when(cte2.isAdHoc()).thenReturn(false);
        when(cte2.usesSharedSchedule()).thenReturn(false);
        ComTaskExecution cte3 = mock(ComTaskExecution.class);
        when(cte3.usesSharedSchedule()).thenReturn(true);
        when(cte3.isScheduledManually()).thenReturn(false);
        when(cte3.isAdHoc()).thenReturn(false);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2, cte3));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    private ScheduledCommunicationTaskAvailable getTestInstance() {
        ScheduledCommunicationTaskAvailable scheduledCommunicationTaskAvailable =
                new ScheduledCommunicationTaskAvailable();
        scheduledCommunicationTaskAvailable.setThesaurus(this.thesaurus);
        return scheduledCommunicationTaskAvailable;
    }
}
