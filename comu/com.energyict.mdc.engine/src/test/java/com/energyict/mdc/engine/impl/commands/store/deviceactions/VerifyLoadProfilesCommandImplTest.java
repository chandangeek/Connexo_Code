package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.engine.TestSerialNumberDeviceIdentifier;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.tasks.LoadProfilesTask;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 15/07/13
 * Time: 11:33
 * Author: khe
 */
public class VerifyLoadProfilesCommandImplTest {

    private final String SERIAL_NUMBER = "mskldjf";

    @Test
    public void testToJournalMessageDescriptionWithTraceLevel () {
        LoadProfileCommand loadProfileCommand = mock(LoadProfileCommand.class);
        LoadProfilesTask task = mock(LoadProfilesTask.class);
        when(task.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(task);
        VerifyLoadProfilesCommandImpl command = new VerifyLoadProfilesCommandImpl(loadProfileCommand, null);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedLoadProfileConfiguration config1 = new DeviceLoadProfileConfiguration(ObisCode.fromString("1.1.1.1.1.1"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        CollectedLoadProfileConfiguration config2 = new DeviceLoadProfileConfiguration(ObisCode.fromString("2.2.2.2.2.2"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.anyList())).thenReturn(Arrays.asList(config1, config2));
        command.doExecute(deviceProtocol, null);
        String description = command.toJournalMessageDescription(LogLevel.TRACE);
        assertThat(description).isEqualTo("VerifyLoadProfilesCommandImpl {executionState: NOT_EXECUTED; completionCode: Ok; nrOfWarnings: 0; nrOfProblems: 0; loadProfileObisCodes: 1.1.1.1.1.1, 2.2.2.2.2.2}");
    }

    @Test
    public void testToJournalMessageDescriptionWithInfoLevel () {
        LoadProfileCommand loadProfileCommand = mock(LoadProfileCommand.class);
        LoadProfilesTask task = mock(LoadProfilesTask.class);
        when(task.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(task);
        VerifyLoadProfilesCommandImpl command = new VerifyLoadProfilesCommandImpl(loadProfileCommand, null);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedLoadProfileConfiguration config1 = new DeviceLoadProfileConfiguration(ObisCode.fromString("1.1.1.1.1.1"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        CollectedLoadProfileConfiguration config2 = new DeviceLoadProfileConfiguration(ObisCode.fromString("2.2.2.2.2.2"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.anyList())).thenReturn(Arrays.asList(config1, config2));
        command.doExecute(deviceProtocol, null);
        String description = command.toJournalMessageDescription(LogLevel.INFO);
        assertThat(description).isEqualTo("VerifyLoadProfilesCommandImpl {executionState: NOT_EXECUTED; completionCode: Ok; loadProfileObisCodes: 1.1.1.1.1.1, 2.2.2.2.2.2}");
    }

    @Test
    public void testToJournalMessageDescriptionWithErrorLevel () {
        LoadProfileCommand loadProfileCommand = mock(LoadProfileCommand.class);
        LoadProfilesTask task = mock(LoadProfilesTask.class);
        when(task.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(task);
        VerifyLoadProfilesCommandImpl command = new VerifyLoadProfilesCommandImpl(loadProfileCommand, null);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedLoadProfileConfiguration config1 = new DeviceLoadProfileConfiguration(ObisCode.fromString("1.1.1.1.1.1"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        CollectedLoadProfileConfiguration config2 = new DeviceLoadProfileConfiguration(ObisCode.fromString("2.2.2.2.2.2"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.anyList())).thenReturn(Arrays.asList(config1, config2));
        command.doExecute(deviceProtocol, null);
        String description = command.toJournalMessageDescription(LogLevel.ERROR);
        assertThat(description).isEqualTo("VerifyLoadProfilesCommandImpl {loadProfileObisCodes: 1.1.1.1.1.1, 2.2.2.2.2.2}");
    }

}