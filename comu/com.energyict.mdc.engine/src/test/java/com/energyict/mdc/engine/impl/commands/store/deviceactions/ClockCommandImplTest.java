package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.ForceClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.SetClockCommand;
import com.energyict.mdc.engine.impl.commands.collect.SynchronizeClockCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;

import com.elster.jupiter.time.TimeDuration;
import org.joda.time.DateTime;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the ClockCommandImpl component
 *
 * @author gna
 * @since 9/05/12 - 10:11
 */
@RunWith(MockitoJUnitRunner.class)
public class ClockCommandImplTest extends CommonCommandImplTests {

    private static final int MINIMUM_CLOCK_DIFFERENCE = 2;
    private static final int MAXIMUM_CLOCK_DIFFERENCE = 10;
    private static final int MAXIMUM_CLOCK_SHIFT = 8;

    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private ComTaskExecution comTaskExecution;

    /**
     * A date parameter which is used in the {@link TimingArgumentMatcher}.
     * If the{@link TimingArgumentMatcher} is used, make sure to set this date to a proper validationDate
     */
    private Date validationDate = new Date();


    /**
     * Get a mocked {@link ClockTask} without a {@link ClockTaskType}
     *
     * @return the mocked ClockTask
     */
    private ClockTask getMockedClockTask() {
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMinimumClockDifference()).thenReturn(Optional.of(new TimeDuration(MINIMUM_CLOCK_DIFFERENCE)));    // minimum 2 second clockDifference
        when(clockTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(MAXIMUM_CLOCK_DIFFERENCE)));   // maximum 10 seconds clockDifference
        when(clockTask.getMaximumClockShift()).thenReturn(Optional.of(new TimeDuration(MAXIMUM_CLOCK_SHIFT)));         // maximum 8 seconds clockShift
        return clockTask;
    }

    private ClockTask getSetClockTask() {
        ClockTask clockTask = getMockedClockTask();
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.SETCLOCK);
        return clockTask;
    }

    private ClockTask getForceClockTask() {
        ClockTask clockTask = getMockedClockTask();
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.FORCECLOCK);
        return clockTask;
    }

    private ClockTask getSynchronizeClockTask() {
        ClockTask clockTask = getMockedClockTask();
        when(clockTask.getClockTaskType()).thenReturn(ClockTaskType.SYNCHRONIZECLOCK);
        return clockTask;
    }

    @Test(expected = CodingException.class)
    public void clockTaskNullTest() {
        new ClockCommandImpl(null, null, comTaskExecution);
        // exception should be thrown because the ClockTask is null
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        new ClockCommandImpl(getSetClockTask(), null, comTaskExecution);
        // exception should be thrown because the TimeDifferenceCommandImpl is null
    }

    @Test
    public void clockCommandSetClockTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        final long timeDifferenceInMillis = 3000L;
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        validationDate = Date.from(frozenClock.instant());
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertThat(((ClockCommandImpl) clockCommand).getClockCommand()).isInstanceOf(SetClockCommand.class);
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        // time difference is between boundaries, should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
        assertThat(clockCommand.getTimeDifference().get()).isEqualTo(new TimeDuration((int) timeDifferenceInMillis, TimeDuration.TimeUnit.MILLISECONDS));
    }

    @Test
    public void setClockCommandAboveMaxTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        validationDate = Date.from(frozenClock.instant());
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.millis() - ((long) MAXIMUM_CLOCK_DIFFERENCE * 1000 + 1000);
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference larger than the max clock diff
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertThat(clockCommand.getIssues()).isNotNull();
        assertThat(clockCommand.getIssues()).hasSize(1);
        assertThat(clockCommand.getWarnings()).hasSize(1);
        assertThat(clockCommand.getProblems()).isEmpty();
        assertThat(clockCommand.getIssues().get(0).isWarning()).isTrue();
        assertThat(clockCommand.getIssues().get(0).getDescription()).isEqualTo("Time difference is larger (11.000) than the maximum defined on the ComTask, setting the time will not be performed");
    }

    @Test
    public void setClockCommandBelowMinTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        validationDate = Date.from(frozenClock.instant());
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.millis() - ((long) MINIMUM_CLOCK_DIFFERENCE * 1000 - 1000);
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference smaller than the min clock diff
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertThat(clockCommand.getIssues()).isNotNull();
        assertThat(clockCommand.getIssues()).isEmpty();
        assertThat(clockCommand.getProblems()).isEmpty();
        assertThat(clockCommand.getWarnings()).isEmpty();
        verify(deviceProtocol, never()).setTime(any(Date.class));
    }

    @Test
    public void setClockCommandWithinBoundaryWithNegativeTimeDiffTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        validationDate = Date.from(frozenClock.instant());
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.millis() + 5000L;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference  in the boundary but negative, clockset should be performed
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertThat(clockCommand.getIssues()).isNotNull();
        assertThat(clockCommand.getIssues()).isEmpty();
        assertThat(clockCommand.getProblems()).isEmpty();
        assertThat(clockCommand.getWarnings()).isEmpty();

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertThat(((ClockCommandImpl) clockCommand).getClockCommand() instanceof SetClockCommand).isTrue();
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        // time difference is between boundaries, should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
        // time difference is between boundaries, should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
    }

    @Test
    public void setClockCommandAboveMaxWithNegativeDiffTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        validationDate = Date.from(frozenClock.instant());
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.millis() + (long) MAXIMUM_CLOCK_DIFFERENCE * 1000 + 1000;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference negative, but larger than the max clock diff
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertThat(clockCommand.getIssues()).isNotNull();
        assertThat(clockCommand.getIssues()).hasSize(1);
        assertThat(clockCommand.getWarnings()).hasSize(1);
        assertThat(clockCommand.getProblems()).isEmpty();
        assertThat(clockCommand.getIssues().get(0).isWarning()).isTrue();
        assertThat(clockCommand.getIssues().get(0).getDescription()).isEqualTo("Time difference is larger (-11.000) than the maximum defined on the ComTask, setting the time will not be performed");
    }

    @Test
    public void setClockCommandBelowMinWithNegativeDiffTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        validationDate = Date.from(frozenClock.instant());
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.millis() + ((long) MINIMUM_CLOCK_DIFFERENCE * 1000 - 1000);
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference negative, but smaller than the max clock diff
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertThat(clockCommand.getIssues()).isNotNull();
        assertThat(clockCommand.getIssues()).isEmpty();
        assertThat(clockCommand.getProblems()).isEmpty();
        assertThat(clockCommand.getWarnings()).isEmpty();
        verify(deviceProtocol, never()).setTime(any(Date.class));
    }

    @Test
    public void clockCommandForceClockTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        validationDate = Date.from(frozenClock.instant());
        ClockTask clockTask = getForceClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());
        assertThat(clockCommand.toJournalMessageDescription(LogLevel.ERROR)).contains("{clockTaskType: FORCECLOCK}");

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertThat(((ClockCommandImpl) clockCommand).getClockCommand() instanceof ForceClockCommand).isTrue();
        // As the ClockCommand is a ForceClockTask, verify that getTime is not called
        verify(deviceProtocol, times(0)).getTime();
        // time difference is outside boundaries, but as the ClockCommand is a ForceClockTask it should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
    }

    @Test
    public void clockCommandSynchronizeClockTest() {
        final long timeDifferenceInMillis = 3000L;
        DateTime now = new DateTime(2012, 5, 1, 10, 52, 13, 111);
        Clock systemTime = Clock.fixed(now.toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(systemTime);
        validationDate = Date.from(systemTime.instant()); // set the validationDate to the meterTime + the clockDifference
        ClockTask clockTask = getSynchronizeClockTask();
        when(deviceProtocol.getTime()).thenReturn(now.minus(timeDifferenceInMillis).toDate()); // 3 seconds time difference
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertThat(((ClockCommandImpl) clockCommand).getClockCommand() instanceof SynchronizeClockCommand).isTrue();
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        // time difference is between boundaries, should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
        // verify the timedifference
        assertThat(clockCommand.getTimeDifference().get()).isEqualTo(new TimeDuration((int) timeDifferenceInMillis, TimeDuration.TimeUnit.MILLISECONDS));
    }

    @Test
    public void clockCommandSynchronizeBelowMinTest() {
        Clock systemTime = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        Clock meterTime = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 12, 111).toDate().toInstant(), ZoneId.systemDefault());   // 1 second behind the system time
        when(commandRootServiceProvider.clock()).thenReturn(systemTime);
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        long deviceTime = systemTime.millis() - ((long) MINIMUM_CLOCK_DIFFERENCE * 1000 - 1000);
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference is smaller than the min difference
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertThat(clockCommand.getIssues()).isNotNull();
        assertThat(clockCommand.getIssues()).hasSize(1);
        assertThat(clockCommand.getWarnings()).hasSize(1);
        assertThat(clockCommand.getProblems()).isEmpty();
        assertThat(clockCommand.getIssues().get(0).isWarning()).isTrue();
        assertThat(clockCommand.getIssues().get(0).getDescription()).isEqualTo("Time difference of 1.000 is smaller that the configured minimum");
    }

    @Test
    public void clockCommandSynchronizeClockWithNegativeDiffTest() {
        final long timeDifferenceInMillis = -3000L;
        Clock systemTime = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        Clock meterTime = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 16, 111).toDate().toInstant(), ZoneId.systemDefault()); // 3 seconds before the system time
        Clock timeToSet = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault()); // we add the clockShift to the timeToSet
        when(commandRootServiceProvider.clock()).thenReturn(systemTime);
        validationDate = Date.from(timeToSet.instant()); // set the validationDate to the meterTime + the clockDifference
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        long deviceTime = systemTime.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertThat(((ClockCommandImpl) clockCommand).getClockCommand() instanceof SynchronizeClockCommand).isTrue();
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        // time difference is between boundaries, should set the Clock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
        // verify the timedifference
        assertThat(clockCommand.getTimeDifference().get()).isEqualTo(new TimeDuration((int) timeDifferenceInMillis, TimeDuration.TimeUnit.MILLISECONDS));
    }

    @Test
    public void clockCommandSynchronizeLargerThenMaxWithNegativeDiffTest() {
        final long timeDifferenceInMillis = -(MAXIMUM_CLOCK_SHIFT * 1000 + 1000);
        Clock systemTime = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        Clock meterTime = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 22, 111).toDate().toInstant(), ZoneId.systemDefault()); // 9 seconds before the system time
        Clock timeToSet = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 14, 111).toDate().toInstant(), ZoneId.systemDefault()); // we add the maximum clockShift to the meterTime
        when(commandRootServiceProvider.clock()).thenReturn(systemTime);
        validationDate = Date.from(timeToSet.instant()); // set the validationDate to the meterTime + the clockDifference
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        long deviceTime = systemTime.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference larger than the max clock diff
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertThat(((ClockCommandImpl) clockCommand).getClockCommand() instanceof SynchronizeClockCommand).isTrue();
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        // time difference is between boundaries, should set the Clock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
        // verify the timedifference
        assertThat(clockCommand.getTimeDifference().get()).isEqualTo(new TimeDuration((int) timeDifferenceInMillis, TimeDuration.TimeUnit.MILLISECONDS));
    }

    @Test
    public void clockCommandSynchronizeBelowMinWithNegativeDifferenceTest() {
        final long timeDifferenceInMillis = -(MINIMUM_CLOCK_DIFFERENCE * 1000 - 1000);
        Clock systemTime = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(systemTime);
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        long deviceTime = systemTime.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference below min difference
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertThat(clockCommand.getIssues()).isNotNull();
        assertThat(clockCommand.getIssues()).hasSize(1);
        assertThat(clockCommand.getWarnings()).hasSize(1);
        assertThat(clockCommand.getProblems()).isEmpty();
        assertThat(clockCommand.getIssues().get(0).isWarning()).isTrue();
        assertThat(clockCommand.getIssues().get(0).getDescription()).isEqualTo("Time difference of -1.000 is smaller that the configured minimum");
    }

    @Test
    public void testJournalMessageDescriptionWithErrorLevel() {
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);

        // Business method
        String description = clockCommand.toJournalMessageDescription(LogLevel.ERROR);

        // Asserts
        assertThat(description).contains("{clockTaskType: SYNCHRONIZECLOCK}");
    }

    @Test
    public void testJournalMessageDescriptionWithInfoLevel() {
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);

        // Business method
        String description = clockCommand.toJournalMessageDescription(LogLevel.INFO);

        // Asserts
        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok; clockTaskType: SYNCHRONIZECLOCK}");
    }

    @Test
    public void testJournalMessageDescriptionWithTraceLevel() {
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);

        // Business method
        String description = clockCommand.toJournalMessageDescription(LogLevel.TRACE);

        // Asserts
        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok; nrOfWarnings: 0; nrOfProblems: 0; clockTaskType: SYNCHRONIZECLOCK; maximumClockShift: 8 seconds; getTimeDifference: }");
    }

    /**
     * Argument Matcher for the setTime
     */
    private class TimingArgumentMatcher extends ArgumentMatcher<Date> {

        /**
         * Returns whether this matcher accepts the given argument.
         * <p/>
         * The method should <b>never</b> assert if the argument doesn't match. It
         * should only return false.
         *
         * @param argument the argument
         * @return whether this matcher accepts the given argument.
         */
        @Override
        public boolean matches(final Object argument) {
            if (argument instanceof Date) {
                Date sTime = (Date) argument;
                return sTime.equals(validationDate);
            }
            return false;
        }
    }
}
