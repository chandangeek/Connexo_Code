/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ClockTaskOptions;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.ClockTask;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SynchronizeClockCommandImplTest extends CommonCommandImplTests {

    @Mock
    private OfflineDevice offlineDevice;

    @Test
    public void getCorrectCommandTypeTest() {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        ClockCommand clockCommand = mock(ClockCommand.class);
        when(clockCommand.getCommandRoot()).thenReturn(groupedDeviceCommand.getCommandRoot());
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        SynchronizeClockCommandImpl synchronizeClockCommand = new SynchronizeClockCommandImpl(groupedDeviceCommand, clockCommand, comTaskExecution);
        assertEquals(ComCommandTypes.SYNCHRONIZE_CLOCK_COMMAND, synchronizeClockCommand.getCommandType());
    }

    @Test
    public void testToJournalMessageDescription() {
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(new TimeDuration(5)));
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(600)));
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(new TimeDuration(111)));
        ClockTaskOptions clockTaskOptions = new ClockTaskOptions(clockTask);
        when(clockCommand.getClockTaskOptions()).thenReturn(clockTaskOptions);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(new TimeDuration(45)));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(groupedDeviceCommand, clockCommand, comTaskExecution);
        String journalMessage = command.toJournalMessageDescription(LogLevel.DEBUG);

        assertEquals(ComCommandDescriptionTitle.SynchronizeClockCommandImpl.getDescription() + " {executionState: NOT_EXECUTED; completionCode: Ok; minimumDifference: 5 seconds; maximumDifference: 600 seconds; maximumClockShift: 111 seconds; timeDifference: 45 seconds}", journalMessage);
    }

    @Test
    public void largerThanMaxDefinedTest() {
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration clockDiff = new TimeDuration(500);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(maxClockDifference));
        ClockTaskOptions clockTaskOptions = new ClockTaskOptions(clockTask);
        when(clockCommand.getClockTaskOptions()).thenReturn(clockTaskOptions);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(groupedDeviceCommand, clockCommand, comTaskExecution);

        // business method
        command.execute(deviceProtocol, newTestExecutionContext());

        assertThat(command.getIssues()).hasSize(1);
        assertThat(command.getWarnings()).hasSize(1);
        assertThat(command.getProblems()).isEmpty();
        assertThat(command.getIssues().get(0).getDescription()).isEqualTo(MessageSeeds.TIME_DIFFERENCE_LARGER_THAN_MAX_DEFINED.getKey());
    }

    @Test
    public void smallerThanMaxClockShiftTest() {
        Clock currentTime = Clock.fixed(new DateTime(2013, 9, 2, 10, 10, 10, 0).toDate().toInstant(), ZoneId.systemDefault());
        when(executionContextServiceProvider.clock()).thenReturn(currentTime);
        when(commandRootServiceProvider.clock()).thenReturn(currentTime);
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration maxClockShift = new TimeDuration(50);
        TimeDuration minClockDifference = new TimeDuration(1);
        TimeDuration clockDiff = new TimeDuration(10);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(maxClockDifference));
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(maxClockShift));
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(minClockDifference));

        ClockTaskOptions clockTaskOptions = new ClockTaskOptions(clockTask);
        when(clockCommand.getClockTaskOptions()).thenReturn(clockTaskOptions);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(groupedDeviceCommand, clockCommand, comTaskExecution);

        // business method
        command.execute(deviceProtocol, this.newTestExecutionContext());

        assertThat(command.getIssues()).isEmpty();
        assertThat(command.getWarnings()).isEmpty();
        assertThat(command.getProblems()).isEmpty();
        verify(deviceProtocol, times(1)).setTime(new DateTime(2013, 9, 2, 10, 10, 10, 0).toDate());
    }

    @Test
    public void largerThanMaxShiftSmallerThanMaxDiffTest() {
        Clock currentTime = Clock.fixed(new DateTime(2013, DateTimeConstants.SEPTEMBER, 2, 10, 10, 10, 0).toDate().toInstant(), ZoneId.systemDefault());
        when(executionContextServiceProvider.clock()).thenReturn(currentTime);
        when(commandRootServiceProvider.clock()).thenReturn(currentTime);
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration maxClockShift = new TimeDuration(50);
        TimeDuration minClockDifference = new TimeDuration(1);
        TimeDuration clockDiff = new TimeDuration(70);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(maxClockDifference));
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(maxClockShift));
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(minClockDifference));

        ClockTaskOptions clockTaskOptions = new ClockTaskOptions(clockTask);
        when(clockCommand.getClockTaskOptions()).thenReturn(clockTaskOptions);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(groupedDeviceCommand, clockCommand, comTaskExecution);

        // business method
        command.execute(deviceProtocol, this.newTestExecutionContext());

        assertThat(command.getIssues()).isEmpty();
        assertThat(command.getWarnings()).isEmpty();
        assertThat(command.getProblems()).isEmpty();
        verify(deviceProtocol, times(1)).setTime(new DateTime(2013, 9, 2, 10, 9, 50, 0).toDate());
    }

    @Test
    public void largerThanMaxShiftSmallerThanMaxDiffButNegativeTest() {
        Clock currentTime = Clock.fixed(new DateTime(2013, 9, 2, 10, 10, 10, 0).toDate().toInstant(), ZoneId.systemDefault());
        when(executionContextServiceProvider.clock()).thenReturn(currentTime);
        when(commandRootServiceProvider.clock()).thenReturn(currentTime);
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration maxClockShift = new TimeDuration(50);
        TimeDuration minClockDifference = new TimeDuration(1);
        TimeDuration clockDiff = new TimeDuration(-70);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(maxClockDifference));
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(maxClockShift));
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(minClockDifference));

        ClockTaskOptions clockTaskOptions = new ClockTaskOptions(clockTask);
        when(clockCommand.getClockTaskOptions()).thenReturn(clockTaskOptions);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(groupedDeviceCommand, clockCommand, comTaskExecution);

        // business method
        command.execute(deviceProtocol, newTestExecutionContext());

        assertThat(command.getIssues()).isEmpty();
        assertThat(command.getProblems()).isEmpty();
        assertThat(command.getWarnings()).isEmpty();
        verify(deviceProtocol, times(1)).setTime(new DateTime(2013, 9, 2, 10, 10, 30, 0).toDate());
    }

    @Test
    public void largerThanMaxShiftLargerThanMaxDiffButNegativeTest() {
        Clock currentTime = Clock.fixed(new DateTime(2013, DateTimeConstants.SEPTEMBER, 2, 10, 10, 10, 0).toDate().toInstant(), ZoneId.systemDefault());
        when(executionContextServiceProvider.clock()).thenReturn(currentTime);
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration maxClockShift = new TimeDuration(50);
        TimeDuration minClockDifference = new TimeDuration(1);
        TimeDuration clockDiff = new TimeDuration(-700);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(maxClockDifference));
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(maxClockShift));
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(minClockDifference));

        ClockTaskOptions clockTaskOptions = new ClockTaskOptions(clockTask);
        when(clockCommand.getClockTaskOptions()).thenReturn(clockTaskOptions);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(groupedDeviceCommand, clockCommand, comTaskExecution);

        // business method
        command.execute(deviceProtocol, this.newTestExecutionContext());

        assertThat(command.getIssues()).hasSize(1);
        assertThat(command.getWarnings()).hasSize(1);
        assertThat(command.getProblems()).isEmpty();
        assertThat(command.getIssues().get(0).getDescription()).isEqualTo(MessageSeeds.TIME_DIFFERENCE_LARGER_THAN_MAX_DEFINED.getKey());
    }

    @Test
    public void smallerThanConfiguredTest() {
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration maxClockShift = new TimeDuration(50);
        TimeDuration minClockDifference = new TimeDuration(2);
        TimeDuration clockDiff = new TimeDuration(1);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(maxClockDifference));
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(maxClockShift));
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(minClockDifference));

        ClockTaskOptions clockTaskOptions = new ClockTaskOptions(clockTask);
        when(clockCommand.getClockTaskOptions()).thenReturn(clockTaskOptions);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(groupedDeviceCommand, clockCommand, comTaskExecution);

        // business method
        command.execute(deviceProtocol, this.newTestExecutionContext());

        assertThat(command.getIssues()).hasSize(1);
        assertThat(command.getIssues().get(0).getDescription()).isEqualTo(MessageSeeds.TIME_DIFFERENCE_BELOW_THAN_MIN_DEFINED.getKey());
        verify(deviceProtocol, never()).setTime(any(Date.class));
    }
}