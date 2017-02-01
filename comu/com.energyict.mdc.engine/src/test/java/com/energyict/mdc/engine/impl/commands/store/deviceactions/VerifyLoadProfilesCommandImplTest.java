/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.engine.TestSerialNumberDeviceIdentifier;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfilesTaskOptions;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.tasks.LoadProfilesTask;

import org.fest.assertions.api.Assertions;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifyLoadProfilesCommandImplTest extends AbstractComCommandExecuteTest {

    private final String SERIAL_NUMBER = "mskldjf";

    @Test
    public void testToJournalMessageDescriptionWithTraceLevel() {
        LoadProfileCommand loadProfileCommand = mock(LoadProfileCommand.class);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(new LoadProfilesTaskOptions());
        VerifyLoadProfilesCommandImpl command = new VerifyLoadProfilesCommandImpl(getGroupedDeviceCommand(), loadProfileCommand);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedLoadProfileConfiguration config1 = new DeviceLoadProfileConfiguration(ObisCode.fromString("1.1.1.1.1.1"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        CollectedLoadProfileConfiguration config2 = new DeviceLoadProfileConfiguration(ObisCode.fromString("2.2.2.2.2.2"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.anyList())).thenReturn(Arrays.asList(config1, config2));
        command.doExecute(deviceProtocol, null);
        String description = command.toJournalMessageDescription(LogLevel.TRACE);
        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok; loadProfileObisCodes: 1.1.1.1.1.1, 2.2.2.2.2.2}");
    }

    @Test
    public void testToJournalMessageDescriptionWithInfoLevel() {
        LoadProfileCommand loadProfileCommand = mock(LoadProfileCommand.class);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(new LoadProfilesTaskOptions());
        VerifyLoadProfilesCommandImpl command = new VerifyLoadProfilesCommandImpl(getGroupedDeviceCommand(), loadProfileCommand);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedLoadProfileConfiguration config1 = new DeviceLoadProfileConfiguration(ObisCode.fromString("1.1.1.1.1.1"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        CollectedLoadProfileConfiguration config2 = new DeviceLoadProfileConfiguration(ObisCode.fromString("2.2.2.2.2.2"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.anyList())).thenReturn(Arrays.asList(config1, config2));
        command.doExecute(deviceProtocol, null);
        String description = command.toJournalMessageDescription(LogLevel.INFO);
        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok; loadProfileObisCodes: 1.1.1.1.1.1, 2.2.2.2.2.2}");
    }

    @Test
    public void testToJournalMessageDescriptionWithErrorLevel() {
        LoadProfileCommand loadProfileCommand = mock(LoadProfileCommand.class);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(new LoadProfilesTaskOptions());
        VerifyLoadProfilesCommandImpl command = new VerifyLoadProfilesCommandImpl(getGroupedDeviceCommand(), loadProfileCommand);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedLoadProfileConfiguration config1 = new DeviceLoadProfileConfiguration(ObisCode.fromString("1.1.1.1.1.1"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        CollectedLoadProfileConfiguration config2 = new DeviceLoadProfileConfiguration(ObisCode.fromString("2.2.2.2.2.2"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.anyList())).thenReturn(Arrays.asList(config1, config2));
        command.doExecute(deviceProtocol, null);
        String description = command.toJournalMessageDescription(LogLevel.ERROR);
        assertThat(description).contains("{loadProfileObisCodes: 1.1.1.1.1.1, 2.2.2.2.2.2}");
    }

    @Test
    public void testToJournalMessageDescription() {
        LoadProfileCommand loadProfileCommand = mock(LoadProfileCommand.class);
        LoadProfilesTask task = mock(LoadProfilesTask.class);
        when(task.failIfLoadProfileConfigurationMisMatch()).thenReturn(true);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(task);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        VerifyLoadProfilesCommandImpl command = new VerifyLoadProfilesCommandImpl(getGroupedDeviceCommand(), loadProfileCommand);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedLoadProfileConfiguration config1 = new DeviceLoadProfileConfiguration(ObisCode.fromString("1.1.1.1.1.1"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        CollectedLoadProfileConfiguration config2 = new DeviceLoadProfileConfiguration(ObisCode.fromString("2.2.2.2.2.2"), new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER));
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.anyList())).thenReturn(Arrays.asList(config1, config2));
        command.doExecute(deviceProtocol, null);
        String journalMessage = command.toJournalMessageDescription(LogLevel.DEBUG);
        Assertions.assertThat(journalMessage).isEqualTo(ComCommandDescriptionTitle.VerifyLoadProfilesCommandImpl.getDescription() + " {executionState: NOT_EXECUTED; completionCode: Ok; loadProfileObisCodes: 1.1.1.1.1.1, 2.2.2.2.2.2}");
    }
}