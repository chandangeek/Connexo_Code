package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.tasks.BasicCheckTask;

import com.elster.jupiter.time.TimeDuration;
import org.joda.time.DateTime;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private BasicCheckTask createCheckTimeDifference() {
        BasicCheckTask basicCheckTask = mock(BasicCheckTask.class);
        when(basicCheckTask.verifyClockDifference()).thenReturn(true);
        when(basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.of(TimeDuration.seconds(30)));
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
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(mock(BasicCheckTask.class), createCommandRoot(), comTaskExecution);
        assertThat(basicCheckCommand.getCommandType()).isEqualTo(ComCommandTypes.BASIC_CHECK_COMMAND);
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
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(),ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        final long timeDifferenceInMillis = 3000L;
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference

        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createCheckTimeDifference(), createCommandRoot(), comTaskExecution);
        basicCheckCommand.execute(deviceProtocol, newTestExecutionContext());

        //asserts
        assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand()).isNotNull();
        assertThat(basicCheckCommand.getBasicCheckTask()).isNotNull();
        // verify that getTime is called only once
        verify(deviceProtocol).getTime();
        assertThat(basicCheckCommand.getTimeDifference().get().getMilliSeconds()).isEqualTo(timeDifferenceInMillis);
        assertThat(basicCheckCommand.getCompletionCode()).isEqualTo(CompletionCode.Ok);
        assertThat(basicCheckCommand.getExecutionState()).isEqualTo(SimpleComCommand.ExecutionState.SUCCESSFULLY_EXECUTED);
        assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getIssues().isEmpty()).isTrue();
        assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getProblems().isEmpty()).isTrue();
        assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getWarnings().isEmpty()).isTrue();
        assertThat(basicCheckCommand.getVerifySerialNumberCommand()).isNull();

        assertThat(basicCheckCommand.toJournalMessageDescription(LogLevel.ERROR)).contains("{readClockDifference: true; getTimeDifference: 3000 milliseconds}");
    }

    @Test
    public void verifyTimeDifferenceOutsideBoundariesTest() {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        when(commandRootServiceProvider.clock()).thenReturn(frozenClock);
        long timeDifferenceInMillis = 60000L;
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 60 seconds time difference

        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createCheckTimeDifference(), createCommandRoot(), comTaskExecution);

        try {
            basicCheckCommand.execute(deviceProtocol, newTestExecutionContext());
        } catch (DeviceConfigurationException e) {
            verify(deviceProtocol).getTime();
            assertThat(basicCheckCommand.getExecutionState()).isEqualTo(SimpleComCommand.ExecutionState.FAILED);
            assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getCompletionCode()).isEqualTo(CompletionCode.TimeError);
            assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getIssues()).hasSize(1);
            assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getProblems()).hasSize(1);
            assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getWarnings()).isEmpty();
            assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand().getTimeDifference().get()).isEqualTo(TimeDuration.seconds(60));
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
        assertThat(basicCheckCommand.getVerifySerialNumberCommand()).isNotNull();
        assertThat(basicCheckCommand.getBasicCheckTask()).isNotNull();
        // verify that the getDeviceSerialNumber is called
        verify(deviceProtocol).getSerialNumber();
        assertThat(basicCheckCommand.getVerifyTimeDifferenceCommand()).isNull();
        // the verification should have been successful so the test should not fail
        assertThat(basicCheckCommand.toJournalMessageDescription(LogLevel.ERROR)).contains("{readClockDifference: false; check serial number}");
    }

    @Test
    public void testJournalDescriptionWithErrorLevel () {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        final long timeDifferenceInMillis = 3000L;
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        String correctMeterSerialNumber = "testJournalDescriptionWithErrorLevel";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumberAndCheckTimeDifference(), createCommandRoot(offlineDevice), comTaskExecution);

        // Business method
        String description = basicCheckCommand.toJournalMessageDescription(LogLevel.ERROR);

        // Asserts
        assertThat(description).contains("{readClockDifference: true; check serial number; getTimeDifference: }");
    }

    @Test
    public void testJournalDescriptionWithInfoLevel () {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        final long timeDifferenceInMillis = 3000L;
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        String correctMeterSerialNumber = "testJournalDescriptionWithInfoLevel";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumberAndCheckTimeDifference(), createCommandRoot(offlineDevice), comTaskExecution);

        // Business method
        String description = basicCheckCommand.toJournalMessageDescription(LogLevel.INFO);

        // Asserts
        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok; readClockDifference: true; check serial number; getTimeDifference: }");
    }

    @Test
    public void testJournalDescriptionWithTraceLevel () {
        Clock frozenClock = Clock.fixed(new DateTime(2012, 5, 1, 10, 52, 13, 111).toDate().toInstant(), ZoneId.systemDefault());
        final long timeDifferenceInMillis = 3000L;
        long deviceTime = frozenClock.millis() - timeDifferenceInMillis;
        when(deviceProtocol.getTime()).thenReturn(new Date(deviceTime)); // 3 seconds time difference
        String correctMeterSerialNumber = "testJournalDescriptionWithTraceLevel";
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        when(deviceProtocol.getSerialNumber()).thenReturn(correctMeterSerialNumber);
        BasicCheckCommandImpl basicCheckCommand = new BasicCheckCommandImpl(createVerifySerialNumberAndCheckTimeDifference(), createCommandRoot(offlineDevice), comTaskExecution);

        // Business method
        String description = basicCheckCommand.toJournalMessageDescription(LogLevel.TRACE);

        // Asserts
        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok; nrOfWarnings: 0; nrOfProblems: 0; readClockDifference: true; readClockDifferenceMaximum(s): 111; check serial number; getTimeDifference: }");
    }

}