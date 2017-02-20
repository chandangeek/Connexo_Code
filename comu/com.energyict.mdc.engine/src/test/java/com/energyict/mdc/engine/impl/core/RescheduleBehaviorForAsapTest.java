/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RescheduleBehaviorForAsapTest extends AbstractRescheduleBehaviorTest {

    public static final long COMPORT_POOL_ID = 1;
    public static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    public static final long CONNECTION_TASK_ID = COMPORT_ID + 1;
    public static final long DEVICE_ID = CONNECTION_TASK_ID + 1;
    public static final long PROTOCOL_DIALECT_CONFIG_PROPS_ID = 6516;

    @Mock
    private ComTaskExecutionGroup comTaskExecutionGroup;
    @Mock
    private ScheduledComTaskExecutionGroup scheduledComTaskExecutionGroup;
    @Mock
    private Device device;
    @Mock
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;

    @Before
    public void setUp() {
        when(this.protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIG_PROPS_ID);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTask.applyComWindowIfAny(any(Instant.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return args[0];
        });
    }

    @Test
    public void rescheduleSingleSuccessTest() {
        SimpleComCommand successfulComCommand = mockSuccessfulComCommand();
        CommandRootImpl mockedCommandRoot = createMockedCommandRootWithCommands(successfulComCommand);

        mockedCommandRoot.execute(true);
        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(comServerDAO, connectionTask, mock(Clock.class));

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
        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(comServerDAO, connectionTask, mock(Clock.class));

        rescheduleBehavior.reschedule(mockedCommandRoot);

        // asserts
        verify(comServerDAO, never()).executionFailed(any(ConnectionTask.class));
        verify(comServerDAO, times(1)).executionCompleted(any(ConnectionTask.class));
    }

    @Test
    public void rescheduleSingleFailedComTaskTest() throws SQLException {
        CommandRootImpl mockedCommandRoot = createMockedCommandRootWithCommands();
        GroupedDeviceCommand groupedDeviceCommand = mockedCommandRoot.getOrCreateGroupedDeviceCommand(offlineDevice, deviceProtocol, deviceProtocolSecurityPropertySet);
        mockFailureComCommand(groupedDeviceCommand, comTaskExecution);

        mockedCommandRoot.execute(true);
        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(comServerDAO, connectionTask, mock(Clock.class));

        rescheduleBehavior.reschedule(mockedCommandRoot);

        // asserts
        verify(comServerDAO, never()).executionCompleted(comTaskExecution);
        verify(comServerDAO, times(1)).executionFailed(comTaskExecution);
        verify(comServerDAO, never()).executionFailed(connectionTask);
        verify(comServerDAO, times(1)).executionCompleted(connectionTask);
    }

    @Test
    public void rescheduleDueToConnectionSetupErrorTest() {
        ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder = mock(ComTaskExecutionSessionBuilder.class);
        when(this.comSessionBuilder.addComTaskExecutionSession(eq(comTaskExecution), eq(comTask), any(Instant.class))).thenReturn(comTaskExecutionSessionBuilder);

        SimpleComCommand successfulComCommand = mockSuccessfulComCommand();
        CommandRootImpl mockedCommandRoot = createMockedCommandRootWithCommands(successfulComCommand);
        mockedCommandRoot.execute(false);

        Clock clock = Clock.fixed(new DateTime(2016, 4, 5, 10, 0, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock clockPlus5Min = Clock.fixed(new DateTime(2016, 4, 5, 10, 5, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(comServerDAO, connectionTask, clock);

        when(connectionTask.getNextExecutionTimestamp()).thenReturn(clock.instant());
        when(connectionTask.getCurrentRetryCount()).thenReturn(1);
        rescheduleBehavior.reschedule(mockedCommandRoot);

        // asserts
        verify(comServerDAO, never()).executionCompleted(any(ComTaskExecution.class));
        verify(comServerDAO, times(1)).executionRescheduled(comTaskExecution, clockPlus5Min.instant()); // we want the comTask to be rescheduled in ASAP
        verify(comServerDAO, never()).executionFailed(any(ComTaskExecution.class));
        verify(comServerDAO, times(1)).executionFailed(connectionTask);

        verify(comTaskExecutionSessionBuilder).addComCommandJournalEntry(any(Instant.class), eq(CompletionCode.NotExecuted), anyString(), anyString());
    }

    @Test
    public void rescheduleDueToConnectionSetupErrorWithoutScheduleOnComTaskExecTest() {
        ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder = mock(ComTaskExecutionSessionBuilder.class);
        when(this.comSessionBuilder.addComTaskExecutionSession(eq(comTaskExecution), eq(comTask), any(Instant.class))).thenReturn(comTaskExecutionSessionBuilder);

        SimpleComCommand successfulComCommand = mockSuccessfulComCommand();
        CommandRootImpl mockedCommandRoot = createMockedCommandRootWithCommands(successfulComCommand);
        mockedCommandRoot.execute(false);

        Clock clock = Clock.fixed(new DateTime(2016, 4, 5, 10, 0, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock clockPlus5Min = Clock.fixed(new DateTime(2016, 4, 5, 10, 5, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(comServerDAO, connectionTask, clock);

        when(connectionTask.getNextExecutionTimestamp()).thenReturn(clock.instant());
        when(connectionTask.getCurrentRetryCount()).thenReturn(0);
        when(connectionTask.lastExecutionFailed()).thenReturn(true);
        rescheduleBehavior.reschedule(mockedCommandRoot);

        // asserts
        verify(comServerDAO, never()).executionCompleted(any(ComTaskExecution.class));
        verify(comServerDAO, times(1)).executionRescheduled(comTaskExecution, null); // we want the comTask to its own schedule (none)
        verify(comServerDAO, never()).executionFailed(any(ComTaskExecution.class));
        verify(comServerDAO, times(1)).executionFailed(connectionTask);

        verify(comTaskExecutionSessionBuilder).addComCommandJournalEntry(any(Instant.class), eq(CompletionCode.NotExecuted), anyString(), anyString());
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

        Clock clock = Clock.fixed(new DateTime(2016, 4, 5, 10, 0, 0, 0).toDate().toInstant(), ZoneId.systemDefault());
        Clock clockPlus5Min = Clock.fixed(new DateTime(2016, 4, 5, 10, 5, 0, 0).toDate().toInstant(), ZoneId.systemDefault());

        RescheduleBehaviorForAsap rescheduleBehavior = new RescheduleBehaviorForAsap(comServerDAO, connectionTask, clock);

        when((connectionTask).getNextExecutionTimestamp()).thenReturn(clock.instant());
        when(connectionTask.getCurrentRetryCount()).thenReturn(1);
        rescheduleBehavior.reschedule(mockedCommandRoot);

        // asserts
        verify(comServerDAO, times(1)).executionCompleted(comTaskExecution1);
        verify(comServerDAO, times(1)).executionCompleted(comTaskExecution2);
        verify(comServerDAO, times(1)).executionCompleted(comTaskExecution3);
        verify(comServerDAO, times(1)).executionRescheduled(comTaskExecution4, clockPlus5Min.instant()); // we want the comTask to be rescheduled in ASAP
        verify(comServerDAO, times(1)).executionRescheduled(comTaskExecution5, clockPlus5Min.instant()); // we want the comTask to be rescheduled in ASAP
        verify(comServerDAO, times(1)).executionFailed(connectionTask);
    }
}