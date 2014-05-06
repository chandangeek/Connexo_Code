package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.comserver.commands.core.SimpleComCommand;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.device.data.journal.CompletionCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.journal.CompletionCode;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.tasks.BasicCheckTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.test.MockEnvironmentTranslations;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.comserver.commands.deviceactions.BasicCheckCommandImpl} component
 *
 * @author gna
 * @since 11/06/12 - 12:12
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicCheckCommandImplTest extends CommonCommandImplTests {

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private ComTaskExecution comTaskExecution;

    @After
    public void resetTimeFactory() throws SQLException {
        Clocks.resetAll();
    }

    private BasicCheckTask createCheckTimeDifference() {
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.verifyClockDifference()).thenReturn(true);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(TimeDuration.seconds(30));
        return basicCheckTask;
    }

    private BasicCheckTask createVerifySerialNumber() {
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.verifySerialNumber()).thenReturn(true);
        return basicCheckTask;
    }

    private BasicCheckTask createVerifySerialNumberAndCheckTimeDifference() {
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.verifySerialNumber()).thenReturn(true);
        when(basicCheckTask.verifyClockDifference()).thenReturn(true);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(new TimeDuration(111));
        return basicCheckTask;
    }

    @Test
    public void getCorrectCommandTypeTest() {
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(mock(BasicCheckTask.class), createCommandRoot(), comTaskExecution);
        assertEquals(ComCommandTypes.BASIC_CHECK_COMMAND, basicCheckCommand.getCommandType());
    }

    @Test(expected = CodingException.class)
    public void basicCheckTaskNullTest() {
        new BasicCheckCommandImpl(null, createCommandRoot(), comTaskExecution);
        // should have gotten the exception
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        new BasicCheckCommandImpl(mock(BasicCheckTask.class), null, comTaskExecution);
        // should have gotten the exception
    }

    @Test
    public void verifyTimeDifferenceWithinBoundariesTest() {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        final long timeDifferenceInMillis = 3000L;
        Clocks.setAppServerClock(frozenClock);
        long deviceTime = frozenClock.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference

        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createCheckTimeDifference(), createCommandRoot(), comTaskExecution);
        basicCheckCommand.execute(deviceProtocol, newTestExecutionContext());

        //asserts
        assertNotNull(basicCheckCommand.getVerifyTimeDifferenceCommand());
        assertNotNull(basicCheckCommand.getBasicCheckTask());
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        assertEquals(timeDifferenceInMillis, basicCheckCommand.getTimeDifference().getMilliSeconds());
        assertEquals(CompletionCode.Ok, basicCheckCommand.getCompletionCode());
        assertEquals(SimpleComCommand.ExecutionState.SUCCESSFULLY_EXECUTED, basicCheckCommand.getExecutionState());
        assertTrue(basicCheckCommand.getVerifyTimeDifferenceCommand().getIssues().isEmpty());
        assertTrue(basicCheckCommand.getVerifyTimeDifferenceCommand().getProblems().isEmpty());
        assertTrue(basicCheckCommand.getVerifyTimeDifferenceCommand().getWarnings().isEmpty());
        assertNull(basicCheckCommand.getVerifySerialNumberCommand());

        assertEquals("BasicCheckCommandImpl {readClockDifference: true; getTimeDifference: 3000 milliseconds}", basicCheckCommand.toJournalMessageDescription(LogLevel.ERROR));
    }

    @Test
    public void verifyTimeDifferenceOutsideBoundariesTest() {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        long timeDifferenceInMillis = 60000L;
        Clocks.setAppServerClock(frozenClock);
        long deviceTime = frozenClock.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 60 seconds time difference

        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createCheckTimeDifference(), createCommandRoot(), comTaskExecution);

        try {
            basicCheckCommand.execute(deviceProtocol, newTestExecutionContext());
        } catch (DeviceConfigurationException e) {
            verify(deviceProtocol).getTime();
            assertEquals("Expecting the tho have execution state FAILED, cause exceeding time difference.", SimpleComCommand.ExecutionState.FAILED, basicCheckCommand.getExecutionState());
            assertEquals("Expecting the VerifyTimeDifferenceCommand to have completionCode TimeError.", CompletionCode.TimeError, basicCheckCommand.getVerifyTimeDifferenceCommand().getCompletionCode());
            assertEquals("Expecting the VerifyTimeDifferenceCommand to have 1 issue.", 1, basicCheckCommand.getVerifyTimeDifferenceCommand().getIssues().size());
            assertEquals("Expecting the VerifyTimeDifferenceCommand to have 1 problem.", 1, basicCheckCommand.getVerifyTimeDifferenceCommand().getProblems().size());
            assertEquals("Expecting the VerifyTimeDifferenceCommand to have no warnings.", 0, basicCheckCommand.getVerifyTimeDifferenceCommand().getWarnings().size());
            assertEquals("Expecting a time difference of 60s.", TimeDuration.seconds(60), basicCheckCommand.getVerifyTimeDifferenceCommand().getTimeDifference());
        }
    }

    @Test
    public void verifySerialNumberTest() {
        String correctMeterSerialNumber = "CorrectMeterSerialNumber";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumber(), createCommandRoot(offlineDevice), comTaskExecution);
        basicCheckCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertNotNull(basicCheckCommand.getVerifySerialNumberCommand());
        assertNotNull(basicCheckCommand.getBasicCheckTask());
        // verify that the getSerialNumber is called
        verify(deviceProtocol).getSerialNumber();
        assertNull(basicCheckCommand.getVerifyTimeDifferenceCommand());
        // the verification should have been successful so the test should not fail
        assertEquals("BasicCheckCommandImpl {readClockDifference: false; check serial number}", basicCheckCommand.toJournalMessageDescription(LogLevel.ERROR));
    }

    @Test
    public void testJournalDescriptionWithErrorLevel () {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        final long timeDifferenceInMillis = 3000L;
        Clocks.setAppServerClock(frozenClock);
        long deviceTime = frozenClock.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        String correctMeterSerialNumber = "testJournalDescriptionWithErrorLevel";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumberAndCheckTimeDifference(), createCommandRoot(offlineDevice), comTaskExecution);

        // Business method
        String description = basicCheckCommand.toJournalMessageDescription(LogLevel.ERROR);

        // Asserts
        Assertions.assertThat(description).isEqualTo("BasicCheckCommandImpl {readClockDifference: true; check serial number; getTimeDifference: }");
    }

    @Test
    public void testJournalDescriptionWithInfoLevel () {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        final long timeDifferenceInMillis = 3000L;
        Clocks.setAppServerClock(frozenClock);
        long deviceTime = frozenClock.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        String correctMeterSerialNumber = "testJournalDescriptionWithInfoLevel";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumberAndCheckTimeDifference(), createCommandRoot(offlineDevice), comTaskExecution);

        // Business method
        String description = basicCheckCommand.toJournalMessageDescription(LogLevel.INFO);

        // Asserts
        Assertions.assertThat(description).isEqualTo("BasicCheckCommandImpl {executionState: NOT_EXECUTED; completionCode: Ok; readClockDifference: true; check serial number; getTimeDifference: }");
    }

    @Test
    public void testJournalDescriptionWithTraceLevel () {
        FrozenClock frozenClock = FrozenClock.frozenOn(2012, Calendar.MAY, 1, 10, 52, 13, 111);
        final long timeDifferenceInMillis = 3000L;
        Clocks.setAppServerClock(frozenClock);
        long deviceTime = frozenClock.now().getTime() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        String correctMeterSerialNumber = "testJournalDescriptionWithTraceLevel";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumberAndCheckTimeDifference(), createCommandRoot(offlineDevice), comTaskExecution);

        // Business method
        String description = basicCheckCommand.toJournalMessageDescription(LogLevel.TRACE);

        // Asserts
        Assertions.assertThat(description).isEqualTo("BasicCheckCommandImpl {executionState: NOT_EXECUTED; completionCode: Ok; nrOfWarnings: 0; nrOfProblems: 0; readClockDifference: true; readClockDifferenceMaximum(s): 111; check serial number; getTimeDifference: }");
    }

}