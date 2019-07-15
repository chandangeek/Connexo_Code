package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
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
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SuccessfulCommunicationTaskExecuted} component
 */
@RunWith(MockitoJUnitRunner.class)
public class SuccessfulCommunicationTaskExecutedTest {

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private Device device;
    @Mock
    private State state;


    @Test
    public void deviceWithoutCommunicationTasks() {
        SuccessfulCommunicationTaskExecuted microCheck = this.getTestInstance();
        when(this.device.getComTaskExecutions()).thenReturn(Collections.emptyList());

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void deviceWithOnlyExecutedCommunicationTasks() {
        SuccessfulCommunicationTaskExecuted microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.getLastSuccessfulCompletionTimestamp()).thenReturn(Instant.now());
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte2.getLastSuccessfulCompletionTimestamp()).thenReturn(Instant.now());
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithOneExecutedCommunicationTask() {
        SuccessfulCommunicationTaskExecuted microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.getLastSuccessfulCompletionTimestamp()).thenReturn(Instant.now());
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithoutExecutedCommunicationTasks() {
        SuccessfulCommunicationTaskExecuted microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte2.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void deviceWithMixOfCommunicationTasks() {
        SuccessfulCommunicationTaskExecuted microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.getLastSuccessfulCompletionTimestamp()).thenReturn(Instant.now());
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte2.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        ComTaskExecution cte3 = mock(ComTaskExecution.class);
        when(cte3.getLastSuccessfulCompletionTimestamp()).thenReturn(null);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2, cte3));

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    private SuccessfulCommunicationTaskExecuted getTestInstance() {
        SuccessfulCommunicationTaskExecuted successfulCommunicationTaskExecuted =
                new SuccessfulCommunicationTaskExecuted();
        successfulCommunicationTaskExecuted.setThesaurus(this.thesaurus);
        return successfulCommunicationTaskExecuted;
    }
}