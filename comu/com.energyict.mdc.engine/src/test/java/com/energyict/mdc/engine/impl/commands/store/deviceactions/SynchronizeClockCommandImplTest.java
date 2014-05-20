package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ClockTask;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.Assertions.assertThat;
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

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Test
    public void getCorrectCommandTypeTest() {
        ClockCommand clockCommand = mock(ClockCommand.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        SynchronizeClockCommandImpl synchronizeClockCommand = new SynchronizeClockCommandImpl(clockCommand, createCommandRoot(), comTaskExecution);
        Assert.assertEquals(ComCommandTypes.SYNCHRONIZE_CLOCK_COMMAND, synchronizeClockCommand.getCommandType());
    }

    @Test
    public void testToJournalMessageDescription () {
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockShift()).thenReturn(new TimeDuration(111));
        when(clockCommand.getClockTask()).thenReturn(clockTask);
        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, commandRoot, null);
        assertEquals("SynchronizeClockCommandImpl {maximumClockShift: 111 seconds}", command.toJournalMessageDescription(LogLevel.ERROR));
    }

    @Test
    public void largerThanMaxDefinedTest() {
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration clockDiff = new TimeDuration(500);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(maxClockDifference);
        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(clockDiff);
        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, commandRoot, null);

        // business method
        command.execute(deviceProtocol, AbstractComCommandExecuteTest.newTestExecutionContext());

        assertThat(command.getIssues()).hasSize(1);
        assertThat(command.getWarnings()).hasSize(1);
        assertThat(command.getProblems()).isEmpty();
        assertThat(command.getIssues().get(0).getDescription()).isEqualTo(MessageFormat.format(Environment.DEFAULT.get().getTranslation("timediffXlargerthanmaxdefined").replaceAll("'", "''"), clockDiff.getMilliSeconds()));
    }

    @Test
    public void smallerThanMaxClockShiftTest() {
        Clock currentTime = new ProgrammableClock().frozenAt(new DateTime(2013, 9, 2, 10, 10, 10, 0).toDate());
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration maxClockShift = new TimeDuration(50);
        TimeDuration minClockDifference = new TimeDuration(1);
        TimeDuration clockDiff = new TimeDuration(10);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(maxClockDifference);
        when(clockTask.getMaximumClockShift()).thenReturn(maxClockShift);
        when(clockTask.getMinimumClockDifference()).thenReturn(minClockDifference);

        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(clockDiff);
        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, commandRoot, null);

        // business method
        command.execute(deviceProtocol, AbstractComCommandExecuteTest.newTestExecutionContext());

        assertThat(command.getIssues()).isEmpty();
        assertThat(command.getWarnings()).isEmpty();
        assertThat(command.getProblems()).isEmpty();
        verify(deviceProtocol, times(1)).setTime(new DateTime(2013, 9, 2, 10, 10, 10, 0).toDate());
    }

    @Test
    public void largerThanMaxShiftSmallerThanMaxDiffTest() {
        Clock currentTime = new ProgrammableClock().frozenAt(new DateTime(2013, Calendar.SEPTEMBER, 2, 10, 10, 10, 0).toDate());
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration maxClockShift = new TimeDuration(50);
        TimeDuration minClockDifference = new TimeDuration(1);
        TimeDuration clockDiff = new TimeDuration(70);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(maxClockDifference);
        when(clockTask.getMaximumClockShift()).thenReturn(maxClockShift);
        when(clockTask.getMinimumClockDifference()).thenReturn(minClockDifference);

        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(clockDiff);
        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, commandRoot, null);

        // business method
        command.execute(deviceProtocol, AbstractComCommandExecuteTest.newTestExecutionContext());

        assertThat(command.getIssues()).isEmpty();
        assertThat(command.getWarnings()).isEmpty();
        assertThat(command.getProblems()).isEmpty();
        verify(deviceProtocol, times(1)).setTime(new DateTime(2013, 9, 2, 10, 9, 50, 0).toDate());
    }

    @Test
    public void largerThanMaxShiftSmallerThanMaxDiffButNegativeTest() {
        Clock currentTime = new ProgrammableClock().frozenAt(new DateTime(2013, 9, 2, 10, 10, 10, 0).toDate());
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration maxClockShift = new TimeDuration(50);
        TimeDuration minClockDifference = new TimeDuration(1);
        TimeDuration clockDiff = new TimeDuration(-70);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(maxClockDifference);
        when(clockTask.getMaximumClockShift()).thenReturn(maxClockShift);
        when(clockTask.getMinimumClockDifference()).thenReturn(minClockDifference);

        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(clockDiff);
        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, commandRoot, null);

        // business method
        command.execute(deviceProtocol, AbstractComCommandExecuteTest.newTestExecutionContext());

        assertThat(command.getIssues()).isEmpty();
        assertThat(command.getProblems()).isEmpty();
        assertThat(command.getWarnings()).isEmpty();
        verify(deviceProtocol, times(1)).setTime(new DateTime(2013, 9, 2, 10, 10, 30, 0).toDate());
    }

    @Test
    public void largerThanMaxShiftLargerThanMaxDiffButNegativeTest() {
        Clock currentTime = new ProgrammableClock().frozenAt(new DateTime(2013, Calendar.SEPTEMBER, 2, 10, 10, 10, 0).toDate());
        TimeDuration maxClockDifference = new TimeDuration(111);
        TimeDuration maxClockShift = new TimeDuration(50);
        TimeDuration minClockDifference = new TimeDuration(1);
        TimeDuration clockDiff = new TimeDuration(-700);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        ClockCommand clockCommand = mock(ClockCommand.class);
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMaximumClockDifference()).thenReturn(maxClockDifference);
        when(clockTask.getMaximumClockShift()).thenReturn(maxClockShift);
        when(clockTask.getMinimumClockDifference()).thenReturn(minClockDifference);

        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(clockDiff);
        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, commandRoot, null);

        // business method
        command.execute(deviceProtocol, AbstractComCommandExecuteTest.newTestExecutionContext());

        assertThat(command.getIssues()).hasSize(1);
        assertThat(command.getWarnings()).hasSize(1);
        assertThat(command.getProblems()).isEmpty();
        assertThat(command.getIssues().get(0).getDescription()).isEqualTo(MessageFormat.format(Environment.DEFAULT.get().getTranslation("timediffXlargerthanmaxdefined").replaceAll("'", "''"), clockDiff.getMilliSeconds()));
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
        when(clockTask.getMaximumClockDifference()).thenReturn(maxClockDifference);
        when(clockTask.getMaximumClockShift()).thenReturn(maxClockShift);
        when(clockTask.getMinimumClockDifference()).thenReturn(minClockDifference);

        when(clockCommand.getClockTask()).thenReturn(clockTask);
        when(clockCommand.getTimeDifference()).thenReturn(clockDiff);
        CommandRoot commandRoot = mock(CommandRoot.class);
        when(commandRoot.getTimeDifferenceCommand(clockCommand, null)).thenReturn(mock(TimeDifferenceCommand.class));
        SynchronizeClockCommandImpl command = new SynchronizeClockCommandImpl(clockCommand, commandRoot, null);

        // business method
        command.execute(deviceProtocol, AbstractComCommandExecuteTest.newTestExecutionContext());

        assertThat(command.getIssues()).hasSize(1);
        assertThat(command.getIssues().get(0).getDescription()).isEqualTo(MessageFormat.format(Environment.DEFAULT.get().getTranslation("timediffXbelowthanmindefined").replaceAll("'", "''"), clockDiff.getMilliSeconds()));
        verify(deviceProtocol, never()).setTime(any(Date.class));
    }
}