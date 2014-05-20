package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.history.CompletionCode;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the BasicCheckCommandImpl component
 *
 * @author gna
 * @since 11/06/12 - 12:12
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicCheckCommandImplTest extends CommonCommandImplTests {

//    @ClassRule
//    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private ComTaskExecution comTaskExecution;

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
        Clock frozenClock = new ProgrammableClock().frozenAt(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate());
        final long timeDifferenceInMillis = 3000L;
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
        Clock frozenClock = new ProgrammableClock().frozenAt(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate());
        long timeDifferenceInMillis = 60000L;
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
        Clock frozenClock = new ProgrammableClock().frozenAt(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate());
        final long timeDifferenceInMillis = 3000L;
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
        assertThat(description).isEqualTo("BasicCheckCommandImpl {readClockDifference: true; check serial number; getTimeDifference: }");
    }

    @Test
    public void testJournalDescriptionWithInfoLevel () {
        Clock frozenClock = new ProgrammableClock().frozenAt(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate());
        final long timeDifferenceInMillis = 3000L;
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
        assertThat(description).isEqualTo("BasicCheckCommandImpl {executionState: NOT_EXECUTED; completionCode: Ok; readClockDifference: true; check serial number; getTimeDifference: }");
    }

    @Test
    public void testJournalDescriptionWithTraceLevel () {
        Clock frozenClock = new ProgrammableClock().frozenAt(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate());
        final long timeDifferenceInMillis = 3000L;
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
        assertThat(description).isEqualTo("BasicCheckCommandImpl {executionState: NOT_EXECUTED; completionCode: Ok; nrOfWarnings: 0; nrOfProblems: 0; readClockDifference: true; readClockDifferenceMaximum(s): 111; check serial number; getTimeDifference: }");
    }

}