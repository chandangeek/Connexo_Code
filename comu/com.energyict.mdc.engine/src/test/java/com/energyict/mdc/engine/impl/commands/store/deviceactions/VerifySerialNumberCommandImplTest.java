package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.test.MockEnvironmentTranslations;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.comserver.commands.deviceactions.VerifySerialNumberCommandImpl} component
 *
 * @author gna
 * @since 11/06/12 - 11:59
 */
@RunWith(MockitoJUnitRunner.class)
public class VerifySerialNumberCommandImplTest extends CommonCommandImplTests {

    private static final String CORRECT_METER_SERIAL_NUMBER = "CorrectMeterSerialNumber";
    private static final String INCORRECT_METER_SERIAL_NUMBER = "IncorrectMeterSerialNumber";

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Test
    public void getCorrectCommandTypeTest() {
        VerifySerialNumberCommandImpl verifySerialNumberCommand = new VerifySerialNumberCommandImpl(mock(OfflineDevice.class), createCommandRoot());
        assertEquals(ComCommandTypes.VERIFY_SERIAL_NUMBER_COMMAND, verifySerialNumberCommand.getCommandType());
    }

    @Test
    public void verifyWithCorrectSerialNumberTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(CORRECT_METER_SERIAL_NUMBER);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSerialNumber()).thenReturn(CORRECT_METER_SERIAL_NUMBER);
        VerifySerialNumberCommandImpl verifySerialNumberCommand = new VerifySerialNumberCommandImpl(offlineDevice, createCommandRoot(offlineDevice));
        verifySerialNumberCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertEquals("There should be no issues logged", 0, verifySerialNumberCommand.getIssues().size());
        assertEquals("There should be no problems logged", 0, verifySerialNumberCommand.getProblems().size());
        assertEquals("There should be no warning logged", 0, verifySerialNumberCommand.getWarnings().size());
    }

    @Test(expected = DeviceConfigurationException.class)
    public void verifyIncorrectSerialNumberTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(CORRECT_METER_SERIAL_NUMBER);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSerialNumber()).thenReturn(INCORRECT_METER_SERIAL_NUMBER);
        VerifySerialNumberCommandImpl verifySerialNumberCommand = new VerifySerialNumberCommandImpl(offlineDevice, createCommandRoot(offlineDevice));
        verifySerialNumberCommand.execute(deviceProtocol, newTestExecutionContext());

        // we should have gotten the DeviceConfigurationException
    }

    @Test
    public void shouldGetWarningForMeterProtocolTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter deviceProtocol = mock(MeterProtocolAdapter.class);
        VerifySerialNumberCommandImpl verifySerialNumberCommand = new VerifySerialNumberCommandImpl(offlineDevice, createCommandRoot(offlineDevice));
        verifySerialNumberCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        List<Issue> issues = verifySerialNumberCommand.getIssues();
        assertEquals(
                "There should be a warning that the SerialNumber could not be checked for an OLD MeterProtocol, we didn't have that interface call yet...",
                1, issues.size());
        assertEquals(
                "There should be a warning that the SerialNumber could not be checked for an OLD MeterProtocol, we didn't have that interface call yet...",
                1, verifySerialNumberCommand.getWarnings().size());
        assertEquals("There should be no problems logged", 0, verifySerialNumberCommand.getProblems().size());
        Assertions.assertThat(issues.get(0).getSource()).isEqualTo(deviceProtocol);
    }

}