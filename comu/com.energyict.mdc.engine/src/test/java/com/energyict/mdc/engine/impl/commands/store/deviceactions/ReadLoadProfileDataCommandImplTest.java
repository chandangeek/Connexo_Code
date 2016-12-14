package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLoadProfileDataCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifierType;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the ReadLoadProfileDataCommandImpl component
 *
 * @author gna
 * @since 31/05/12 - 11:12
 */
public class ReadLoadProfileDataCommandImplTest extends CommonCommandImplTests {

    @Mock
    private OfflineDevice offlineDevice;

    private static DeviceLoadProfile createDeviceCollectedLoadProfile() {
        DeviceLoadProfile deviceLoadProfile = new DeviceLoadProfile(new SimpleLoadProfileIdentifier());
        List<IntervalData> intervalDatas = new ArrayList<>();
        List<ChannelInfo> channelInfos = new ArrayList<>();
        channelInfos.add(new ChannelInfo(channelInfos.size(), "CHN1", Unit.get(BaseUnit.VOLT)));
        channelInfos.add(new ChannelInfo(channelInfos.size(), "CHN2", Unit.get(BaseUnit.AMPERE)));
        for (int i = 0; i < 10; i++) {
            intervalDatas.add(new IntervalData(new Date(), new HashSet<>()));
        }
        deviceLoadProfile.setCollectedIntervalData(intervalDatas, channelInfos);
        return deviceLoadProfile;
    }

    @Test
    public void doExecuteTest() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getLoadProfileData(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.<CollectedLoadProfile>asList(createDeviceCollectedLoadProfile()));
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(ObisCode.fromString("1.1.1.1.1.1"));
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));
        ExecutionContext executionContext = newTestExecutionContext();
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn("MyMrid");
        when(comTaskExecution.getDevice()).thenReturn(device);
        LoadProfileCommand loadProfileCommand = groupedDeviceCommand.getLoadProfileCommand(loadProfilesTask, groupedDeviceCommand, comTaskExecution);
        ReadLoadProfileDataCommand readLoadProfileDataCommand = groupedDeviceCommand.getReadLoadProfileDataCommand(loadProfileCommand, comTaskExecution);
        readLoadProfileDataCommand.execute(deviceProtocol, executionContext);
        String journalMessage = readLoadProfileDataCommand.toJournalMessageDescription(LogLevel.INFO);

        // asserts
        assertNotNull("There should be some collected data", loadProfileCommand.getCollectedData());
        assertEquals("There should be 1 collected data object in the list", 1, loadProfileCommand.getCollectedData().size());
        assertTrue("The collected data should be CollectedLoadProfile", loadProfileCommand.getCollectedData().get(0) instanceof CollectedLoadProfile);
        assertEquals("Should have 10 intervals", 10, ((CollectedLoadProfile) loadProfileCommand.getCollectedData().get(0)).getCollectedIntervalData().size());
        assertThat(journalMessage).startsWith(ComCommandDescriptionTitle.ReadLoadProfileDataCommandImpl.getDescription() + " {collectedProfiles: (0.0.99.98.0.255 - Supported - channels: CHN1, CHN2 - dataPeriod: [");
    }

    private static class SimpleLoadProfileIdentifier implements LoadProfileIdentifier {
        @Override
        public LoadProfile getLoadProfile() {
            return null;
        }

        @Override
        public LoadProfileIdentifierType getLoadProfileIdentifierType() {
            return null;
        }

        @Override
        public List<Object> getParts() {
            return null;
        }

        @Override
        public DeviceIdentifier getDeviceIdentifier() {
            return null;
        }

        @Override
        public String toString() {
            return getProfileObisCode().toString();
        }

        @Override
        public ObisCode getProfileObisCode() {
            return ObisCode.fromString("0.0.99.98.0.255");
        }
    }
}