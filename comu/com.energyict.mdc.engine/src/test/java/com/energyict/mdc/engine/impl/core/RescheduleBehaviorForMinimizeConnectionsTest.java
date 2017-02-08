/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.tasks.ComTask;

import org.joda.time.DateTime;

import java.time.Clock;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RescheduleBehaviorForMinimizeConnectionsTest extends AbstractRescheduleBehaviorTest {

    private static final long COMPORT_POOL_ID = 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    private static final int MAX_NUMBER_OF_TRIES = 3;

    @Mock
    private ComTaskExecutionGroup comTaskExecutionGroup;
    @Mock
    private ComTask comTask;

    @Before
    public void setUp() {
        when(this.comTask.getMaxNumberOfTries()).thenReturn(MAX_NUMBER_OF_TRIES);
    }

    @Test
    public void rescheduleSingleSuccessTest() {
        SimpleComCommand successfulComCommand = mockSuccessfulComCommand();
        CommandRootImpl mockedCommandRoot = createMockedCommandRootWithCommands(successfulComCommand);

        mockedCommandRoot.execute(true);
        RescheduleBehaviorForMinimizeConnections rescheduleBehavior = new RescheduleBehaviorForMinimizeConnections(comServerDAO, connectionTask, mock(Clock.class));

        rescheduleBehavior.reschedule(mockedCommandRoot);

        // asserts
        verify(comServerDAO, times(1)).executionCompleted(comTaskExecution);
        verify(comServerDAO, never()).executionFailed(any(ComTaskExecution.class));
        verify(comServerDAO, never()).executionFailed(any(ConnectionTask.class));
    }

    @Test
    public void rescheduleSuccessFulConnectionTaskTest() {
        SimpleComCommand successfulComCommand = mockSuccessfulComCommand();
        CommandRootImpl mockedCommandRoot = createMockedCommandRootWithCommands(successfulComCommand);

        mockedCommandRoot.execute(true);
        RescheduleBehaviorForMinimizeConnections rescheduleBehavior = new RescheduleBehaviorForMinimizeConnections(comServerDAO, connectionTask, mock(Clock.class));

        rescheduleBehavior.reschedule(mockedCommandRoot);

        // asserts
        verify(comServerDAO, never()).executionFailed(any(ConnectionTask.class));
        verify(comServerDAO, times(1)).executionCompleted(any(ConnectionTask.class));
    }

    @Test
    public void rescheduleSingleFailedComTaskTest() {
        CommandRootImpl mockedCommandRoot = createMockedCommandRootWithCommands();
        GroupedDeviceCommand groupedDeviceCommand = mockedCommandRoot.getOrCreateGroupedDeviceCommand(offlineDevice, deviceProtocol, deviceProtocolSecurityPropertySet);
        mockFailureComCommand(groupedDeviceCommand, comTaskExecution);

        mockedCommandRoot.execute(true);
        RescheduleBehaviorForMinimizeConnections rescheduleBehavior = new RescheduleBehaviorForMinimizeConnections(comServerDAO, connectionTask, mock(Clock.class));

        rescheduleBehavior.reschedule(mockedCommandRoot);

        // asserts
        verify(comServerDAO, never()).executionCompleted(any(ComTaskExecution.class));
        verify(comServerDAO, times(1)).executionFailed(comTaskExecution);
        verify(comServerDAO, times(1)).executionFailed(any(ConnectionTask.class));
        verify(comServerDAO, never()).executionCompleted(any(ConnectionTask.class));
    }

    @Test
    public void rescheduleDueToConnectionSetupErrorTest() {
        SimpleComCommand notExecutedComCommand = mockNotExecutedComCommand();
        CommandRootImpl mockedCommandRoot = createMockedCommandRootWithCommands(notExecutedComCommand);

        mockedCommandRoot.execute(false);

        Clock clock = Clock.fixed(new DateTime(2016, 4, 5, 10, 0, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        RescheduleBehaviorForMinimizeConnections rescheduleBehavior = new RescheduleBehaviorForMinimizeConnections(comServerDAO, connectionTask, clock);
        when(connectionTask.getNextExecutionTimestamp()).thenReturn(clock.instant());

        rescheduleBehavior.reschedule(mockedCommandRoot);

        // asserts
        verify(comServerDAO, never()).executionCompleted(any(ComTaskExecution.class));
        verify(comServerDAO, times(1)).executionRescheduled(comTaskExecution, clock.instant());
        verify(comServerDAO, never()).executionFailed(any(ComTaskExecution.class)); // we DONT want the comTask to be rescheduled in minimize
        verify(comServerDAO, times(1)).executionFailed(connectionTask);
    }

    @Test
    public void rescheduleDueToConnectionBrokenDuringExecutionTest() {
        SimpleComCommand notExecutedComCommand = mockNotExecutedComCommand();
        SimpleComCommand successfulComCommand1 = mockSuccessfulComCommand();
        SimpleComCommand successfulComCommand2 = mockSuccessfulComCommand();
        SimpleComCommand successfulComCommand3 = mockSuccessfulComCommand();
        ComTaskExecution comTaskExecution1 = mockNewComTaskExecution();
        ComTaskExecution comTaskExecution2 = mockNewComTaskExecution();
        ComTaskExecution comTaskExecution3 = mockNewComTaskExecution();
        ComTaskExecution comTaskExecution4 = mockNewComTaskExecution();
        ComTaskExecution comTaskExecution5 = mockNewComTaskExecution();

        CommandRootImpl mockedCommandRoot = createMockedCommandRootWithPairs(
                Pair.of(successfulComCommand1, comTaskExecution1),
                Pair.of(successfulComCommand2, comTaskExecution2),
                Pair.of(successfulComCommand3, comTaskExecution3));

        GroupedDeviceCommand groupedDeviceCommand = mockedCommandRoot.getOrCreateGroupedDeviceCommand(offlineDevice, deviceProtocol, deviceProtocolSecurityPropertySet);
        mockConnectionErrorFailureComCommand(groupedDeviceCommand, comTaskExecution4);
        groupedDeviceCommand.addCommand(notExecutedComCommand, comTaskExecution5);

        mockedCommandRoot.execute(true);
        Clock clock = Clock.fixed(new DateTime(2016, 7, 7, 7, 0, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        RescheduleBehaviorForMinimizeConnections rescheduleBehavior = new RescheduleBehaviorForMinimizeConnections(comServerDAO, connectionTask, clock);
        when(connectionTask.getNextExecutionTimestamp()).thenReturn(clock.instant());

        rescheduleBehavior.reschedule(mockedCommandRoot);

        // asserts
        verify(comServerDAO, times(1)).executionCompleted(comTaskExecution1);
        verify(comServerDAO, times(1)).executionCompleted(comTaskExecution2);
        verify(comServerDAO, times(1)).executionCompleted(comTaskExecution3);
        verify(comServerDAO, times(1)).executionFailed(comTaskExecution4);
        verify(comServerDAO, times(1)).executionRescheduled(comTaskExecution5, clock.instant());
        verify(comServerDAO, times(1)).executionFailed(connectionTask);
    }
}
