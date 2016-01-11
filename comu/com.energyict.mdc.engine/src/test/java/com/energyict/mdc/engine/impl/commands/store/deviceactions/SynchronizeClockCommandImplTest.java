package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.ClockTask;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 9/07/13
 * Time: 10:17
 * Author: khe
 */
@RunWith(MockitoJUnitRunner.class)
public class SynchronizeClockCommandImplTest extends CommonCommandImplTests {

    @Test
    public void getCorrectCommandTypeTest() {
        ClockCommand clockCommand = mock(ClockCommand.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        SynchronizeClockCommandImpl synchronizeClockCommand = new SynchronizeClockCommandImpl(clockCommand, createCommandRoot(), comTaskExecution);
        assertThat(synchronizeClockCommand.getCommandType()).isEqualTo(ComCommandTypes.SYNCHRONIZE_CLOCK_COMMAND);
    }

    @Test
    public void testToJournalMessageDescription () {
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.empty());
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.empty());
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(new TimeDuration(111)));
        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.empty());
        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider commandRootServiceProvider = mock(CommandRoot.ServiceProvider.class);
        IssueService issueService = executionContextServiceProvider.issueService();
        when(commandRootServiceProvider.issueService()).thenReturn(issueService);
        Clock clock = executionContextServiceProvider.clock();
        when(commandRootServiceProvider.clock()).thenReturn(clock);
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);
        when(commandRoot.findOrCreateTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, createCommandRoot(), null);
        assertThat(command.toJournalMessageDescription(LogLevel.DEBUG)).contains("maximumClockShift: 111 seconds");
    }

    @Test
    public void largerThanMaxDefinedTest() {
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration clockDiff = new TimeDuration(500);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.empty());
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(maxClockDifference));
        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider commandRootServiceProvider = mock(CommandRoot.ServiceProvider.class);
        IssueService issueService = executionContextServiceProvider.issueService();
        when(commandRootServiceProvider.issueService()).thenReturn(issueService);
        Clock clock = executionContextServiceProvider.clock();
        when(commandRootServiceProvider.clock()).thenReturn(clock);
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);
        when(commandRoot.findOrCreateTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, createCommandRoot(), null);

        // business method
        command.execute(deviceProtocol, this.newTestExecutionContext());

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

        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, createCommandRoot(), null);

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

        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, createCommandRoot(), null);

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

        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, createCommandRoot(), null);

        // business method
        command.execute(deviceProtocol, this.newTestExecutionContext());

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

        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider commandRootServiceProvider = mock(CommandRoot.ServiceProvider.class);
        IssueService issueService = executionContextServiceProvider.issueService();
        when(commandRootServiceProvider.issueService()).thenReturn(issueService);
        Clock clock = executionContextServiceProvider.clock();
        when(commandRootServiceProvider.clock()).thenReturn(clock);
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);
        when(commandRoot.findOrCreateTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, createCommandRoot(), null);

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

        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(Optional.of(clockDiff));
        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider commandRootServiceProvider = mock(CommandRoot.ServiceProvider.class);
        IssueService issueService = executionContextServiceProvider.issueService();
        when(commandRootServiceProvider.issueService()).thenReturn(issueService);
        Clock clock = executionContextServiceProvider.clock();
        when(commandRootServiceProvider.clock()).thenReturn(clock);
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);
        when(commandRoot.findOrCreateTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, createCommandRoot(), null);

        // business method
        command.execute(deviceProtocol, this.newTestExecutionContext());

        assertThat(command.getIssues()).hasSize(1);
        assertThat(command.getIssues().get(0).getDescription()).isEqualTo(MessageSeeds.TIME_DIFFERENCE_BELOW_THAN_MIN_DEFINED.getKey());
        verify(deviceProtocol, never()).setTime(any(Date.class));
    }

}