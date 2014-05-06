package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.comserver.commands.core.CommandRootImpl;
import com.energyict.comserver.core.JobExecution;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.commands.LoadProfileCommand;
import com.energyict.mdc.commands.ReadLoadProfileDataCommand;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.meterdata.DeviceLoadProfile;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.tasks.LoadProfilesTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.LoadProfilesTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Matchers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.comserver.commands.deviceactions.ReadLoadProfileDataCommandImpl} component
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
        JobExecution.ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(mock(OfflineDevice.class), executionContext, issueService);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        LoadProfileCommand loadProfileCommand = commandRoot.getLoadProfileCommand(loadProfilesTask, commandRoot, comTaskExecution);
        ReadLoadProfileDataCommand readLoadProfileDataCommand = commandRoot.getReadLoadProfileDataCommand(loadProfileCommand, comTaskExecution);
        readLoadProfileDataCommand.execute(deviceProtocol, executionContext);
        Assertions.assertThat(readLoadProfileDataCommand.toJournalMessageDescription(LogLevel.ERROR)).startsWith("ReadLoadProfileDataCommandImpl {loadProfileObisCodes: 1.1.1.1.1.1");

        // asserts
        assertNotNull("There should be some collected data", loadProfileCommand.getCollectedData());
        Assert.assertEquals("There should be 1 collected data object in the list", 1, loadProfileCommand.getCollectedData().size());
        assertTrue("The collected data should be CollectedLoadProfile", loadProfileCommand.getCollectedData().get(0) instanceof CollectedLoadProfile);
        Assert.assertEquals("Should have 10 intervals", 10, ((CollectedLoadProfile) loadProfileCommand.getCollectedData().get(0)).getCollectedIntervalData().size());
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