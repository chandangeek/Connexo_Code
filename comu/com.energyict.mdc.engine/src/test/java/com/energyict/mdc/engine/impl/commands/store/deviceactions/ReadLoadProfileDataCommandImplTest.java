package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLoadProfileDataCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LoadProfilesTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.*;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the ReadLoadProfileDataCommandImpl component
 *
 * @author gna
 * @since 31/05/12 - 11:12
 */
public class ReadLoadProfileDataCommandImplTest extends CommonCommandImplTests {

    @Test
    public void doExecuteTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getLoadProfileData(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.<CollectedLoadProfile>asList(createDeviceCollectedLoadProfile()));
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(ObisCode.fromString("1.1.1.1.1.1"));
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));
        ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(mock(OfflineDevice.class), executionContext, this.commandRootServiceProvider);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn("MyMrid");
        when(comTaskExecution.getDevice()).thenReturn(device);
        LoadProfileCommand loadProfileCommand = commandRoot.getLoadProfileCommand(loadProfilesTask, commandRoot, comTaskExecution);
        ReadLoadProfileDataCommand readLoadProfileDataCommand = commandRoot.getReadLoadProfileDataCommand(loadProfileCommand, comTaskExecution);
        readLoadProfileDataCommand.execute(deviceProtocol, executionContext);
        assertThat(readLoadProfileDataCommand.toJournalMessageDescription(LogLevel.ERROR)).contains("{loadProfileObisCodes: 1.1.1.1.1.1");

        // asserts
        assertThat(loadProfileCommand.getCollectedData()).isNotNull();
        assertThat(loadProfileCommand.getCollectedData()).hasSize(1);
        assertThat(loadProfileCommand.getCollectedData().get(0)).isInstanceOf(CollectedLoadProfile.class);
        assertThat(((CollectedLoadProfile) loadProfileCommand.getCollectedData().get(0)).getCollectedIntervalData()).hasSize(10);
    }

    private static DeviceLoadProfile createDeviceCollectedLoadProfile() {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(null);
        List<IntervalData> intervalDatas = new ArrayList<>();
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            intervalDatas.add(new IntervalData(new Date(), IntervalStateBits.OK));
        }
        deviceLoadProfile.setCollectedData(intervalDatas, channelInfos);
        return deviceLoadProfile;
    }

}