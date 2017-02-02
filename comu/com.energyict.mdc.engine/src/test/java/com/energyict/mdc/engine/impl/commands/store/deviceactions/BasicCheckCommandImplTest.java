package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.BasicComCommandBehavior;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.tasks.BasicCheckTask;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
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

    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private OfflineDevice offlineDevice;

    private BasicCheckTask createCheckTimeDifference() {
        return createCheckTimeDifference(30);
    }

    private BasicCheckTask createCheckTimeDifference(int maximumClockDifference) {
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.verifyClockDifference()).thenReturn(true);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.of(TimeDuration.seconds(maximumClockDifference)));
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
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.of(new TimeDuration(111)));
        return basicCheckTask;
    }

    @Test
    public void getCorrectCommandTypeTest() {
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(mock(BasicCheckTask.class), createGroupedDeviceCommand(offlineDevice, deviceProtocol), comTaskExecution);
        assertThat(basicCheckCommand.getCommandType()).isEqualTo(ComCommandTypes.BASIC_CHECK_COMMAND);
    }

    @Test(expected = CodingException.class)
    public void basicCheckTaskNullTest() {
        new BasicCheckCommandImpl(null, createGroupedDeviceCommand(offlineDevice, deviceProtocol), comTaskExecution);
        // should have gotten the exception
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        new BasicCheckCommandImpl(mock(BasicCheckTask.class), null, comTaskExecution);
        // should have gotten the exception
    }

    @Test
    public void verifyTimeDifferenceWithinBoundariesTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        final long timeDifferenceInMillis = 3000L;
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference

        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createCheckTimeDifference(), createGroupedDeviceCommand(offlineDevice, deviceProtocol), comTaskExecution);
        basicCheckCommand.execute(deviceProtocol, newTestExecutionContext());
        String journalMessage = basicCheckCommand.toJournalMessageDescription(LogLevel.ERROR);

        //asserts
        assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand()).isNotNull();
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        assertThat(basicCheckCommand.getTimeDifference().get().getMilliSeconds()).isEqualTo(timeDifferenceInMillis);
        assertThat(basicCheckCommand.getCompletionCode()).isEqualTo(CompletionCode.Ok);
        assertThat(basicCheckCommand.getExecutionState()).isEqualTo(BasicComCommandBehavior.ExecutionState.SUCCESSFULLY_EXECUTED);
        assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getIssues().isEmpty()).isTrue();
        assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getProblems().isEmpty()).isTrue();
        assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getWarnings().isEmpty()).isTrue();
        assertThat(basicCheckCommand.getVerifySerialNumberCommand()).isNull();

        assertEquals(ComCommandDescriptionTitle.BasicCheckCommandImpl.getDescription() + " {check maximum clock difference}", journalMessage);
    }

    @Test
    public void verifyTimeDifferenceOutsideBoundariesTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        long timeDifferenceInMillis = 60000L;
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 60 seconds time difference

        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createCheckTimeDifference(), createGroupedDeviceCommand(offlineDevice, deviceProtocol), comTaskExecution);

        try {
            basicCheckCommand.execute(deviceProtocol, newTestExecutionContext());
        } catch (DeviceConfigurationException e) {
            verify(deviceProtocol).getTime();
            assertEquals("Expecting the tho have execution state FAILED, cause exceeding time difference.", BasicComCommandBehavior.ExecutionState.FAILED, basicCheckCommand.getExecutionState());
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
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumber(), createGroupedDeviceCommand(offlineDevice, deviceProtocol), comTaskExecution);
        basicCheckCommand.execute(deviceProtocol, newTestExecutionContext());
        String journalMessage = basicCheckCommand.toJournalMessageDescription(LogLevel.ERROR);

        // asserts
        assertThat(basicCheckCommand.getVerifySerialNumberCommand()).isNotNull();
        // verify that the getDeviceSerialNumber is called
        verify(deviceProtocol).getSerialNumber();
        assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand()).isNull();
        // the verification should have been successful so the test should not fail
        assertEquals(ComCommandDescriptionTitle.BasicCheckCommandImpl.getDescription() + " {check serial number}", journalMessage);
    }

    @Test
    public void testJournalDescriptionWithErrorLevel() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        final long timeDifferenceInMillis = 3000L;
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        String correctMeterSerialNumber = "testJournalDescriptionWithErrorLevel";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumberAndCheckTimeDifference(), createGroupedDeviceCommand(offlineDevice, deviceProtocol), comTaskExecution);

        // Business method
        String description = basicCheckCommand.toJournalMessageDescription(LogLevel.ERROR);

        // Asserts
        assertThat(description).isEqualTo(ComCommandDescriptionTitle.BasicCheckCommandImpl.getDescription() + " {check serial number; check maximum clock difference}");
    }

    @Test
    public void testJournalDescriptionWithInfoLevel() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        final long timeDifferenceInMillis = 3000L;
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        String correctMeterSerialNumber = "testJournalDescriptionWithInfoLevel";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumberAndCheckTimeDifference(), createGroupedDeviceCommand(offlineDevice, deviceProtocol), comTaskExecution);

        // Business method
        String description = basicCheckCommand.toJournalMessageDescription(LogLevel.INFO);

        // Asserts
        assertThat(description).isEqualTo(ComCommandDescriptionTitle.BasicCheckCommandImpl.getDescription() + " {executionState: NOT_EXECUTED; completionCode: Ok; check serial number; check maximum clock difference}");
    }

    @Test
    public void testJournalDescriptionWithTraceLevel() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        final long timeDifferenceInMillis = 3000L;
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        String correctMeterSerialNumber = "testJournalDescriptionWithTraceLevel";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumberAndCheckTimeDifference(), createGroupedDeviceCommand(offlineDevice, deviceProtocol), comTaskExecution);

        // Business method
        String description = basicCheckCommand.toJournalMessageDescription(LogLevel.TRACE);

        // Asserts
        assertThat(description).isEqualTo(ComCommandDescriptionTitle.BasicCheckCommandImpl.getDescription() + " {executionState: NOT_EXECUTED; completionCode: Ok; check serial number; check maximum clock difference}");
    }

    @Test
    public void testUpdateAccordingTo() throws Exception {
        String correctMeterSerialNumber = "CorrectMeterSerialNumber";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);

        Clock frozenClock = Clock.fixed(new DateTime(2012, Calendar.MAY, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        final long timeDifferenceInMillis = 3000L;
        long deviceTime = frozenClock.instant().toEpochMilli() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference

        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumber(), groupedDeviceCommand, comTaskExecution);
        basicCheckCommand.updateAccordingTo(createCheckTimeDifference(), groupedDeviceCommand, comTaskExecution);

        basicCheckCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertNotNull(basicCheckCommand.getVerifySerialNumberCommand());
        // verify that the getSerialNumber is called
        verify(deviceProtocol).getSerialNumber();

        assertNotNull(basicCheckCommand.getVerifyTimeDifferenceCommand());
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        assertEquals(timeDifferenceInMillis, basicCheckCommand.getTimeDifference().get().getMilliSeconds());
        assertEquals(CompletionCode.Ok, basicCheckCommand.getCompletionCode());
        assertEquals(BasicComCommandBehavior.ExecutionState.SUCCESSFULLY_EXECUTED, basicCheckCommand.getExecutionState());
        assertTrue(basicCheckCommand.getVerifyTimeDifferenceCommand().getIssues().isEmpty());
        assertTrue(basicCheckCommand.getVerifyTimeDifferenceCommand().getProblems().isEmpty());
        assertTrue(basicCheckCommand.getVerifyTimeDifferenceCommand().getWarnings().isEmpty());
        // the verification should have been successful so the test should not fail
    }

    @Test
    public void testUpdateAccordingToSmallerTimeDifference() throws Exception {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        int maximumClockDifference_A = 30;
        int maximumClockDifference_B = 15;
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createCheckTimeDifference(maximumClockDifference_A), groupedDeviceCommand, comTaskExecution);
        basicCheckCommand.updateAccordingTo(createCheckTimeDifference(maximumClockDifference_B), groupedDeviceCommand, comTaskExecution);

        // asserts
        assertNull(basicCheckCommand.getVerifySerialNumberCommand());
        assertNotNull(basicCheckCommand.getVerifyTimeDifferenceCommand());
        assertEquals(maximumClockDifference_B, basicCheckCommand.getMaximumClockDifference().get().getSeconds());
    }

    @Test
    public void testUpdateAccordingToLargerTimeDifference() throws Exception {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        int maximumClockDifference_A = 15;
        int maximumClockDifference_B = 30;
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createCheckTimeDifference(maximumClockDifference_A), groupedDeviceCommand, comTaskExecution);
        basicCheckCommand.updateAccordingTo(createCheckTimeDifference(maximumClockDifference_B), groupedDeviceCommand, comTaskExecution);

        // asserts
        assertNull(basicCheckCommand.getVerifySerialNumberCommand());
        assertNotNull(basicCheckCommand.getVerifyTimeDifferenceCommand());
        assertEquals(maximumClockDifference_A, basicCheckCommand.getMaximumClockDifference().get().getSeconds());
    }
}