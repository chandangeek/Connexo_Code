package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.mdc.commands.ClockCommand;
import com.energyict.mdc.commands.ForceClockCommand;
import com.energyict.mdc.commands.SetClockCommand;
import com.energyict.mdc.commands.SynchronizeClockCommand;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.tasks.ClockTask;
import com.energyict.mdc.protocol.tasks.ClockTaskType;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.test.MockEnvironmentTranslations;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.comserver.commands.deviceactions.ClockCommandImpl} component
 *
 * @author gna
 * @since 9/05/12 - 10:11
 */
@RunWith(MockitoJUnitRunner.class)
public class ClockCommandImplTest extends CommonCommandImplTests {

    private static final int MINIMUM_CLOCK_DIFFERENCE = 2;
    private static final int MAXIMUM_CLOCK_DIFFERENCE = 10;
    private static final int MAXIMUM_CLOCK_SHIFT = 8;

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private ComTaskExecution comTaskExecution;

    /**
     * A date parameter which is used in the {@link TimingArgumentMatcher}.
     * If the{@link TimingArgumentMatcher} is used, make sure to set this date to a proper validationDate
     */
    private Date validationDate = new Date();

    @After
    public void resetTimeFactory () throws SQLException {
        Clocks.resetAll();
    }

    /**
     * Get a mocked {@link ClockTask} without a {@link ClockTaskType}
     *
     * @return the mocked ClockTask
     */
    private ClockTask getMockedClockTask() {
        ClockTask clockTask = mock(ClockTask.class);
        when(clockTask.getMinimumClockDifference()).thenReturn(new TimeDuration(MINIMUM_CLOCK_DIFFERENCE));    // minimum 2 second clockDifference
        when(clockTask.getMaximumClockDifference()).thenReturn(new TimeDuration(MAXIMUM_CLOCK_DIFFERENCE));   // maximum 10 seconds clockDifference
        when(clockTask.getMaximumClockShift()).thenReturn(new TimeDuration(MAXIMUM_CLOCK_SHIFT));         // maximum 8 seconds clockShift
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
     public void commandRootNullTest(){
        new ClockCommandImpl(getSetClockTask(), null, comTaskExecution);
        // exception should be thrown because the TimeDifferenceCommandImpl is null
    }

    @Test
    public void clockCommandSetClockTest() {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        final long timeDifferenceInMillis = 3000L;
        Clocks.setAppServerClock(frozenClock);
        validationDate = frozenClock.now();
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertTrue(((ClockCommandImpl) clockCommand).getClockCommand() instanceof SetClockCommand);
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        // time difference is between boundaries, should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
        Assert.assertEquals(new TimeDuration((int) timeDifferenceInMillis, TimeDuration.MILLISECONDS), clockCommand.getTimeDifference());
    }

    @Test
    public void setClockCommandAboveMaxTest() {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        Clocks.setAppServerClock(frozenClock);
        validationDate = frozenClock.now();
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.now().getTime() - ((long) MAXIMUM_CLOCK_DIFFERENCE * 1000 + 1000);
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference larger than the max clock diff
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertNotNull(clockCommand.getIssues());
        Assert.assertEquals("We expect 1 issue", 1, clockCommand.getIssues().size());
        Assert.assertEquals("We expect 1 warning", 1, clockCommand.getWarnings().size());
        Assert.assertEquals("We expect no problems", 0, clockCommand.getProblems().size());
        assertTrue("The issue should be a warning", clockCommand.getIssues().get(0).isWarning());
        Assert.assertEquals(Environment.DEFAULT.get().getTranslation("timediffXlargerthanmaxdefined"), clockCommand.getIssues().get(0).getDescription());
    }

    @Test
    public void setClockCommandBelowMinTest() {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        Clocks.setAppServerClock(frozenClock);
        validationDate = frozenClock.now();
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.now().getTime() - ((long) MINIMUM_CLOCK_DIFFERENCE * 1000 - 1000);
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference smaller than the min clock diff
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertNotNull(clockCommand.getIssues());
        Assertions.assertThat(clockCommand.getIssues()).isEmpty();
        Assertions.assertThat(clockCommand.getProblems()).isEmpty();
        Assertions.assertThat(clockCommand.getWarnings()).isEmpty();
        verify(deviceProtocol, never()).setTime(any(Date.class));
    }

    @Test
    public void setClockCommandWithinBoundaryWithNegativeTimeDiffTest() {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        Clocks.setAppServerClock(frozenClock);
        validationDate = frozenClock.now();
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.now().getTime() + 5000L;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference  in the boundary but negative, clockset should be performed
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertNotNull(clockCommand.getIssues());
        Assert.assertEquals("We expect 0 issues", 0, clockCommand.getIssues().size());
        Assert.assertEquals("We expect 0 problems", 0, clockCommand.getProblems().size());
        Assert.assertEquals("We expect 0 warnings", 0, clockCommand.getWarnings().size());

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertTrue(((ClockCommandImpl) clockCommand).getClockCommand() instanceof SetClockCommand);
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        // time difference is between boundaries, should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
        // time difference is between boundaries, should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
    }

    @Test
    public void setClockCommandAboveMaxWithNegativeDiffTest() {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        Clocks.setAppServerClock(frozenClock);
        validationDate = frozenClock.now();
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.now().getTime() + (long) MAXIMUM_CLOCK_DIFFERENCE * 1000 + 1000;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference negative, but larger than the max clock diff
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertNotNull(clockCommand.getIssues());
        Assert.assertEquals("We expect 1 issue", 1, clockCommand.getIssues().size());
        Assert.assertEquals("We expect 1 warning", 1, clockCommand.getWarnings().size());
        Assert.assertEquals("We expect no problems", 0, clockCommand.getProblems().size());
        assertTrue("The issue should be a warning", clockCommand.getIssues().get(0).isWarning());
        Assert.assertEquals(Environment.DEFAULT.get().getTranslation("timediffXlargerthanmaxdefined"), clockCommand.getIssues().get(0).getDescription());
    }

    @Test
    public void setClockCommandBelowMinWithNegativeDiffTest() {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        Clocks.setAppServerClock(frozenClock);
        validationDate = frozenClock.now();
        ClockTask clockTask = getSetClockTask();
        long deviceTime = frozenClock.now().getTime() +((long) MINIMUM_CLOCK_DIFFERENCE * 1000 - 1000);
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference negative, but smaller than the max clock diff
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertNotNull(clockCommand.getIssues());
        Assertions.assertThat(clockCommand.getIssues()).isEmpty();
        Assertions.assertThat(clockCommand.getProblems()).isEmpty();
        Assertions.assertThat(clockCommand.getWarnings()).isEmpty();
        verify(deviceProtocol, never()).setTime(any(Date.class));
    }

    @Test
    public void clockCommandForceClockTest() {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        Clocks.setAppServerClock(frozenClock);
        validationDate = frozenClock.now();
        ClockTask clockTask = getForceClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());
        Assert.assertEquals("ClockCommandImpl {clockTaskType: FORCECLOCK}", clockCommand.toJournalMessageDescription(LogLevel.ERROR));

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertTrue(((ClockCommandImpl) clockCommand).getClockCommand() instanceof ForceClockCommand);
        // As the ClockCommand is a ForceClockTask, verify that getTime is not called
        verify(deviceProtocol, times(0)).getTime();
        // time difference is outside boundaries, but as the ClockCommand is a ForceClockTask it should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
    }

    @Test
    public void clockCommandSynchronizeClockTest() {
        final long timeDifferenceInMillis = 3000L;
        FrozenClock systemTime = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        FrozenClock meterTime = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 10, 111); // 3 seconds behind the system time
        Clocks.setAppServerClock(systemTime);
        validationDate = systemTime.now(); // set the validationDate to the meterTime + the clockDifference
        ClockTask clockTask = getSynchronizeClockTask();
        long deviceTime = systemTime.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertTrue(((ClockCommandImpl) clockCommand).getClockCommand() instanceof SynchronizeClockCommand);
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        // time difference is between boundaries, should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
        // verify the timedifference
        Assert.assertEquals(new TimeDuration((int) timeDifferenceInMillis, TimeDuration.MILLISECONDS), clockCommand.getTimeDifference());
    }

    @Test
    public void clockCommandSynchronizeBelowMinTest() {
        FrozenClock systemTime = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        FrozenClock meterTime = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 12, 111);   // 1 second behind the system time
        Clocks.setAppServerClock(systemTime);
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        long deviceTime = systemTime.now().getTime() - ((long) MINIMUM_CLOCK_DIFFERENCE * 1000 - 1000);
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference is smaller than the min difference
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertNotNull(clockCommand.getIssues());
        Assert.assertEquals("We expect 1 issue", 1, clockCommand.getIssues().size());
        Assert.assertEquals("We expect 1 warning", 1, clockCommand.getWarnings().size());
        Assert.assertEquals("We expect no problems", 0, clockCommand.getProblems().size());
        assertTrue("The issue should be a warning", clockCommand.getIssues().get(0).isWarning());
        Assert.assertEquals(Environment.DEFAULT.get().getTranslation("timediffXbelowthanmindefined"), clockCommand.getIssues().get(0).getDescription());
    }

    @Test
    public void clockCommandSynchronizeClockWithNegativeDiffTest() {
        final long timeDifferenceInMillis = -3000L;
        FrozenClock systemTime = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        FrozenClock meterTime = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 16, 111); // 3 seconds before the system time
        FrozenClock timeToSet = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111); // we add the clockShift to the timeToSet
        Clocks.setAppServerClock(systemTime );
        validationDate = timeToSet.now(); // set the validationDate to the meterTime + the clockDifference
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        long deviceTime = systemTime.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertTrue(((ClockCommandImpl) clockCommand).getClockCommand() instanceof SynchronizeClockCommand);
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        // time difference is between boundaries, should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
        // verify the timedifference
        Assert.assertEquals(new TimeDuration((int) timeDifferenceInMillis, TimeDuration.MILLISECONDS), clockCommand.getTimeDifference());
    }

    @Test
    public void clockCommandSynchronizeLargerThenMaxWithNegativeDiffTest() {
        final long timeDifferenceInMillis = -(MAXIMUM_CLOCK_SHIFT * 1000 + 1000);
        FrozenClock systemTime = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        FrozenClock meterTime = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 22, 111); // 9 seconds before the system time
        FrozenClock timeToSet = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 14, 111); // we add the maximum clockShift to the meterTime
        Clocks.setAppServerClock(systemTime);
        validationDate = timeToSet.now(); // set the validationDate to the meterTime + the clockDifference
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        long deviceTime = systemTime.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference larger than the max clock diff
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        // the ClockCommand should be a SetClockCommand
        assertTrue(((ClockCommandImpl) clockCommand).getClockCommand() instanceof SynchronizeClockCommand);
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        // time difference is between boundaries, should set the frozenClock time
        verify(deviceProtocol).setTime(Matchers.<Date>argThat(new TimingArgumentMatcher()));
        // verify the timedifference
        Assert.assertEquals(new TimeDuration((int) timeDifferenceInMillis, TimeDuration.MILLISECONDS), clockCommand.getTimeDifference());
    }

    @Test
    public void clockCommandSynchronizeBelowMinWithNegativeDifferenceTest() {
        final long timeDifferenceInMillis = -(MINIMUM_CLOCK_DIFFERENCE * 1000 - 1000);
        FrozenClock systemTime = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        Clocks.setAppServerClock(systemTime);
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);
        long deviceTime = systemTime.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // time difference below min difference
        clockCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertNotNull(clockCommand.getIssues());
        Assert.assertEquals("We expect 1 issue", 1, clockCommand.getIssues().size());
        Assert.assertEquals("We expect 1 warning", 1, clockCommand.getWarnings().size());
        Assert.assertEquals("We expect no problems", 0, clockCommand.getProblems().size());
        assertTrue("The issue should be a warning", clockCommand.getIssues().get(0).isWarning());
        Assert.assertEquals(Environment.DEFAULT.get().getTranslation("timediffXbelowthanmindefined"), clockCommand.getIssues().get(0).getDescription());
    }

    @Test
    public void testJournalMessageDescriptionWithErrorLevel () {
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);

        // Business method
        String description = clockCommand.toJournalMessageDescription(LogLevel.ERROR);

        // Asserts
        Assertions.assertThat(description).isEqualTo("ClockCommandImpl {clockTaskType: SYNCHRONIZECLOCK}");
    }

    @Test
    public void testJournalMessageDescriptionWithInfoLevel () {
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);

        // Business method
        String description = clockCommand.toJournalMessageDescription(LogLevel.INFO);

        // Asserts
        Assertions.assertThat(description).isEqualTo("ClockCommandImpl {executionState: NOT_EXECUTED; completionCode: Ok; clockTaskType: SYNCHRONIZECLOCK}");
    }

    @Test
    public void testJournalMessageDescriptionWithTraceLevel () {
        ClockTask clockTask = getSynchronizeClockTask();
        ClockCommand clockCommand = new ClockCommandImpl(clockTask, createCommandRoot(), comTaskExecution);

        // Business method
        String description = clockCommand.toJournalMessageDescription(LogLevel.TRACE);

        // Asserts
        Assertions.assertThat(description).isEqualTo("ClockCommandImpl {executionState: NOT_EXECUTED; completionCode: Ok; nrOfWarnings: 0; nrOfProblems: 0; clockTaskType: SYNCHRONIZECLOCK; maximumClockShift: 8 seconds; getTimeDifference: }");
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
