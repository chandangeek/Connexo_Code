/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;

import org.fest.assertions.api.Assertions;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link VerifySerialNumberCommandImpl} component.
 *
 * @author gna
 * @since 11/06/12 - 11:59
 */
@RunWith(MockitoJUnitRunner.class)
public class VerifySerialNumberCommandImplTest extends CommonCommandImplTests {

    private static final String CORRECT_METER_SERIAL_NUMBER = "CorrectMeterSerialNumber";
    private static final String INCORRECT_METER_SERIAL_NUMBER = "IncorrectMeterSerialNumber";

    @Mock
    private OfflineDevice offlineDevice;

    @Test
    public void getCorrectCommandTypeTest() {
        VerifySerialNumberCommandImpl verifySerialNumberCommand = new VerifySerialNumberCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol));
        assertEquals(ComCommandTypes.VERIFY_SERIAL_NUMBER_COMMAND, verifySerialNumberCommand.getCommandType());
    }

    @Test
    public void verifyWithCorrectSerialNumberTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(CORRECT_METER_SERIAL_NUMBER);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSerialNumber()).thenReturn(CORRECT_METER_SERIAL_NUMBER);
        VerifySerialNumberCommandImpl verifySerialNumberCommand = new VerifySerialNumberCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol));
        verifySerialNumberCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertEquals("There should be no issues logged", 0, verifySerialNumberCommand.getIssues().size());
        assertEquals("There should be no problems logged", 0, verifySerialNumberCommand.getProblems().size());
        assertEquals("There should be no warning logged", 0, verifySerialNumberCommand.getWarnings().size());
    }

    @Test
    public void verifyIncorrectSerialNumberTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(CORRECT_METER_SERIAL_NUMBER);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSerialNumber()).thenReturn(INCORRECT_METER_SERIAL_NUMBER);
        VerifySerialNumberCommandImpl verifySerialNumberCommand = new VerifySerialNumberCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol));
        verifySerialNumberCommand.execute(deviceProtocol, newTestExecutionContext());

        Assertions.assertThat(verifySerialNumberCommand.getIssues().size()).isEqualTo(1);
        Assertions.assertThat(verifySerialNumberCommand.getIssues().get(0).getDescription()).isEqualTo("serialNumberMismatch");
        Assertions.assertThat(verifySerialNumberCommand.getCompletionCode()).isEqualTo(CompletionCode.ConfigurationError);
    }

    @Test
    public void shouldGetWarningForMeterProtocolTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        DeviceProtocol deviceProtocol = mock(MeterProtocolAdapter.class);
        VerifySerialNumberCommandImpl verifySerialNumberCommand = new VerifySerialNumberCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol));
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
        assertThat(issues.get(0).getSource()).isEqualTo(deviceProtocol);
    }

}