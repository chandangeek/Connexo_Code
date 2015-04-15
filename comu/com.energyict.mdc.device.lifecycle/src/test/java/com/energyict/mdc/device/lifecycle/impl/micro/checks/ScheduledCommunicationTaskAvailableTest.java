package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.nls.Thesaurus;

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
 * Tests the {@link ScheduledCommunicationTaskAvailable} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-15 (09:34)
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
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation.isPresent()).isTrue();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED);
    }

    @Test
    public void deviceWithOnlyAdHocCommunicationTasks() {
        ScheduledCommunicationTaskAvailable microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.isAdHoc()).thenReturn(true);
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte2.isAdHoc()).thenReturn(true);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation.isPresent()).isTrue();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED);
    }

    @Test
    public void deviceWithOneScheduledCommunicationTask() {
        ScheduledCommunicationTaskAvailable microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.isAdHoc()).thenReturn(false);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation.isPresent()).isFalse();
    }

    @Test
    public void deviceWithMixOfCommunicationTasks() {
        ScheduledCommunicationTaskAvailable microCheck = this.getTestInstance();
        ComTaskExecution cte1 = mock(ComTaskExecution.class);
        when(cte1.isAdHoc()).thenReturn(true);
        ComTaskExecution cte2 = mock(ComTaskExecution.class);
        when(cte2.isAdHoc()).thenReturn(false);
        when(this.device.getComTaskExecutions()).thenReturn(Arrays.asList(cte1, cte2));

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device);

        // Asserts
        assertThat(violation.isPresent()).isFalse();
    }

    private ScheduledCommunicationTaskAvailable getTestInstance() {
        return new ScheduledCommunicationTaskAvailable(this.thesaurus);
    }

}