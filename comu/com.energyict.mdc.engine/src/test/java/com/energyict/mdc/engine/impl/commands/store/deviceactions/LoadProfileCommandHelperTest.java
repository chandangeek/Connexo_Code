package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 11/06/2015 - 11:33
 */
public class LoadProfileCommandHelperTest extends CommonCommandImplTests {

    private static final TimeDuration FIXED_LOAD_PROFILE_INTERVAL = new TimeDuration(900);
    private static final String FIXED_DEVICE_SERIAL_NUMBER = "FIXED_DEVICE_SERIAL_NUMBER";
    private static final Unit FIXED_CHANNEL_UNIT = Unit.get("kWh");
    @Mock
    ComTaskExecution comTaskExecution;

    private static OfflineLoadProfileChannel createMockedOfflineLoadProfileChannel(final ObisCode obisCode) {
        OfflineLoadProfileChannel loadProfileChannel = mock(OfflineLoadProfileChannel.class);
        when(loadProfileChannel.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        when(loadProfileChannel.getObisCode()).thenReturn(obisCode);
        when(loadProfileChannel.getUnit()).thenReturn(FIXED_CHANNEL_UNIT);
        when(loadProfileChannel.isStoreData()).thenReturn(true);
        return loadProfileChannel;
    }

    @Before
    public void doBefore() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(0L);
        when(comTaskExecution.getDevice()).thenReturn(device);
    }

    @Test
    public void createLoadProfileReadersTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);

        OfflineLoadProfile offlineLoadProfile1 = mockOfflineLoadProfile(1);
        OfflineLoadProfile offlineLoadProfile2 = mockOfflineLoadProfile(2);
        OfflineLoadProfile offlineLoadProfile3 = mockOfflineLoadProfile(3);
        OfflineLoadProfile offlineLoadProfile4 = mockOfflineLoadProfile(4);

        OfflineLoadProfileChannel mockChannel = getMockChannel();
        when(offlineLoadProfile1.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));
        when(offlineLoadProfile2.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));
        when(offlineLoadProfile3.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));
        when(offlineLoadProfile4.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel));

        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getAllOfflineLoadProfiles()).thenReturn(Arrays.asList(offlineLoadProfile1, offlineLoadProfile2, offlineLoadProfile3, offlineLoadProfile4));

        // Business method
        Map<LoadProfileReader, OfflineLoadProfile> loadProfileReaderMap = new HashMap<>();
        LoadProfileCommandHelper.createLoadProfileReaders(commandRootServiceProvider, loadProfileReaderMap, loadProfilesTask, offlineDevice, comTaskExecution);

        // Asserts
        assertNotNull(loadProfileReaderMap);
        assertEquals("Expected 4 readers in the map", 4, loadProfileReaderMap.size());
    }

    private OfflineLoadProfile mockOfflineLoadProfile(long loadProfileId) {
        OfflineLoadProfile offlineLoadProfile = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile.getLoadProfileId()).thenReturn(loadProfileId);
        return offlineLoadProfile;
    }

    @Test
    public void createChannelInfosTest() {
        ObisCode[] channelObisCodes = {ObisCode.fromString("1.0.1.8.1.255"), ObisCode.fromString("1.0.1.8.2.255"), ObisCode.fromString("1.0.1.8.3.255")};

        OfflineLoadProfileChannel loadProfileChannel1 = createMockedOfflineLoadProfileChannel(channelObisCodes[0]);
        OfflineLoadProfileChannel loadProfileChannel2 = createMockedOfflineLoadProfileChannel(channelObisCodes[1]);
        OfflineLoadProfileChannel loadProfileChannel3 = createMockedOfflineLoadProfileChannel(channelObisCodes[2]);
        OfflineLoadProfile offlineLoadProfile = mock(OfflineLoadProfile.class);
        when(offlineLoadProfile.interval()).thenReturn(FIXED_LOAD_PROFILE_INTERVAL.asTemporalAmount());
        when(offlineLoadProfile.getOfflineChannels()).thenReturn(Arrays.asList(loadProfileChannel1, loadProfileChannel2, loadProfileChannel3));

        OfflineLoadProfileChannel mockChannel1 = getMockChannel(channelObisCodes[0]);
        OfflineLoadProfileChannel mockChannel2 = getMockChannel(channelObisCodes[1]);
        OfflineLoadProfileChannel mockChannel3 = getMockChannel(channelObisCodes[2]);
        when(offlineLoadProfile.getOfflineChannels()).thenReturn(Arrays.asList(mockChannel1, mockChannel2, mockChannel3));

        // Asserts
        List<ChannelInfo> channelInfos = LoadProfileCommandHelper.createChannelInfos(offlineLoadProfile, comTaskExecution);
        assertNotNull(channelInfos);
        assertEquals("Expected three channels", 3, channelInfos.size());

        int count = 0;
        for (ChannelInfo channelInfo : channelInfos) {
            assertEquals(channelInfo.getId(), count);
            assertEquals(channelInfo.getName(), channelObisCodes[count++].toString());
            assertEquals(channelInfo.getUnit(), FIXED_CHANNEL_UNIT);
            assertEquals(channelInfo.getMeterIdentifier(), FIXED_DEVICE_SERIAL_NUMBER);
        }
    }

    private OfflineLoadProfileChannel getMockChannel() {
        return getMockChannel(ObisCode.fromString("1.x.128.1.0.255"));
    }

    private OfflineLoadProfileChannel getMockChannel(ObisCode obisCode) {
        OfflineLoadProfileChannel mockChannel = mock(OfflineLoadProfileChannel.class);
        when(mockChannel.isStoreData()).thenReturn(true);
        when(mockChannel.getObisCode()).thenReturn(obisCode);
        when(mockChannel.getUnit()).thenReturn(FIXED_CHANNEL_UNIT);
        when(mockChannel.getMasterSerialNumber()).thenReturn(FIXED_DEVICE_SERIAL_NUMBER);
        return mockChannel;
    }

}