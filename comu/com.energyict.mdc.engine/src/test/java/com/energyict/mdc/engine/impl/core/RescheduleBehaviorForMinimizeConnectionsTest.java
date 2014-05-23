package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.tasks.ComTask;

import java.util.Arrays;
import java.util.Collections;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.RescheduleBehaviorForMinimizeConnections} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/06/13
 * Time: 10:20
 */
@RunWith(MockitoJUnitRunner.class)
public class RescheduleBehaviorForMinimizeConnectionsTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final int MAX_NUMBER_OF_TRIES = 3;

    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private ScheduledComTaskExecutionGroup scheduledComTaskExecutionGroup;
    @Mock
    private ScheduledConnectionTask connectionTask;
    @Mock
    private ComTask comTask;

    @Before
    public void setUp() {
        when(this.comTask.getMaxNumberOfTries()).thenReturn(MAX_NUMBER_OF_TRIES);
    }

    @Test
    public void rescheduleSingleSuccessTest() {
        ComTaskExecution successfulComTaskExecution = createMockedComTaskExecution();

        RescheduleBehaviorForMinimizeConnections rescheduleBehavior = new RescheduleBehaviorForMinimizeConnections(
                comServerDAO, Arrays.<ComTaskExecution>asList(successfulComTaskExecution),
                Collections.<ComTaskExecution>emptyList(), Collections.<ComTaskExecution>emptyList(),
                connectionTask);

        rescheduleBehavior.performRescheduling(RescheduleBehavior.RescheduleReason.COMTASKS);

        // asserts
        verify(comServerDAO, times(1)).executionCompleted(Arrays.asList(successfulComTaskExecution));
        verify(comServerDAO, never()).executionFailed(any(ComTaskExecution.class));
        verify(comServerDAO, never()).executionFailed(any(ConnectionTask.class));
    }

    @Test
    public void rescheduleSuccessFulConnectionTaskTest() {
        ComTaskExecution successfulComTaskExecution = createMockedComTaskExecution();

        RescheduleBehaviorForMinimizeConnections rescheduleBehavior = new RescheduleBehaviorForMinimizeConnections(
                comServerDAO, Arrays.<ComTaskExecution>asList(successfulComTaskExecution),
                Collections.<ComTaskExecution>emptyList(), Collections.<ComTaskExecution>emptyList(),
                connectionTask);

        rescheduleBehavior.performRescheduling(RescheduleBehavior.RescheduleReason.COMTASKS);

        // asserts
        verify(comServerDAO, never()).executionFailed(any(ConnectionTask.class));
        verify(comServerDAO, times(1)).executionCompleted(any(ConnectionTask.class));
    }

    private ComTaskExecution createMockedComTaskExecution() {
        final ComTaskExecution mock = mock(ComTaskExecution.class);
        when(mock.getComTask()).thenReturn(comTask);
        return mock;
    }

    @Test
    public void rescheduleSingleFailedComTaskTest() {
        ComTaskExecution failedComTaskExecution = createMockedComTaskExecution();

        RescheduleBehaviorForMinimizeConnections rescheduleBehavior = new RescheduleBehaviorForMinimizeConnections(
                comServerDAO, Collections.<ComTaskExecution>emptyList(),
                Arrays.<ComTaskExecution>asList(failedComTaskExecution), Collections.<ComTaskExecution>emptyList(),
                connectionTask);

        rescheduleBehavior.performRescheduling(RescheduleBehavior.RescheduleReason.COMTASKS);

        // asserts
        verify(comServerDAO, never()).executionCompleted(any(ComTaskExecution.class));
        verify(comServerDAO, times(1)).executionFailed(Arrays.asList(failedComTaskExecution));
        verify(comServerDAO, times(1)).executionFailed(any(ConnectionTask.class));
        verify(comServerDAO, never()).executionCompleted(any(ConnectionTask.class));
    }

    @Test
    public void rescheduleDueToConnectionSetupErrorTest() {
        ComTaskExecution notExecutedComTaskExecution = createMockedComTaskExecution();

        RescheduleBehaviorForMinimizeConnections rescheduleBehavior = new RescheduleBehaviorForMinimizeConnections(
                comServerDAO,
                Collections.<ComTaskExecution>emptyList(),
                Collections.<ComTaskExecution>emptyList(),
                Arrays.<ComTaskExecution>asList(notExecutedComTaskExecution),
                connectionTask);

        rescheduleBehavior.performRescheduling(RescheduleBehavior.RescheduleReason.CONNECTION_SETUP);

        // asserts
        verify(comServerDAO, never()).executionCompleted(any(ComTaskExecution.class));
        verify(comServerDAO, never()).executionFailed(any(ComTaskExecution.class)); // we DONT want the comTask to be rescheduled in minimize
        verify(comServerDAO, times(1)).executionFailed(connectionTask);
    }

    @Test
    public void rescheduleDueToConnectionBrokenDuringExecutionTest() {
        ComTaskExecution notExecutedComTaskExecution = createMockedComTaskExecution();
        ComTaskExecution failedComTaskExecution = createMockedComTaskExecution();
        ComTaskExecution successfulComTaskExecution1 = createMockedComTaskExecution();
        ComTaskExecution successfulComTaskExecution2 = createMockedComTaskExecution();
        ComTaskExecution successfulComTaskExecution3 = createMockedComTaskExecution();

        RescheduleBehaviorForMinimizeConnections rescheduleBehavior = new RescheduleBehaviorForMinimizeConnections(
                comServerDAO,
                Arrays.<ComTaskExecution>asList(successfulComTaskExecution1, successfulComTaskExecution2, successfulComTaskExecution3),
                Arrays.<ComTaskExecution>asList(failedComTaskExecution),
                Arrays.<ComTaskExecution>asList(notExecutedComTaskExecution),
                connectionTask);

        rescheduleBehavior.performRescheduling(RescheduleBehavior.RescheduleReason.CONNECTION_BROKEN);

        // asserts
        verify(comServerDAO, times(1)).executionCompleted(Arrays.asList(successfulComTaskExecution1, successfulComTaskExecution2, successfulComTaskExecution3));
        verify(comServerDAO, times(1)).executionFailed(Arrays.asList(failedComTaskExecution));
        verify(comServerDAO, never()).executionFailed(notExecutedComTaskExecution);     // the notExecutedComTask remains untouched
        verify(comServerDAO, never()).executionCompleted(notExecutedComTaskExecution);  // the notExecutedComTask remains untouched
        verify(comServerDAO, times(1)).executionFailed(connectionTask);
    }
}
