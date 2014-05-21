package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.masterdata.LoadProfileType;

import java.util.Arrays;
import java.util.Date;

import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link OfflineLoadProfileImpl} component
 *
 * @author gna
 * @since 30/05/12 - 14:40
 */
public class OfflineLoadProfileImplTest {

    private static final TimeDuration PROFILE_INTERVAL = new TimeDuration(1, TimeDuration.DAYS);
    private static final Date LAST_READING = new Date(1338381863000L);
    private static final long RTU_ID = 4565;
    private static final int LOAD_PROFILE_ID = 48564;
    private static final long LOAD_PROFILE_TYPE_ID = 11565;
    private static final String MASTER_SERIAL_NUMBER = "Master_SerialNumber";

    private static LoadProfile getNewMockedLoadProfile(final long id, final ObisCode obisCode) {
        Device rtu = mock(Device.class);
        when(rtu.getSerialNumber()).thenReturn(MASTER_SERIAL_NUMBER);
        when(rtu.getId()).thenReturn(RTU_ID);
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getDeviceObisCode()).thenReturn(obisCode);
        when(loadProfileSpec.getInterval()).thenReturn(PROFILE_INTERVAL);
        LoadProfileType type = mock(LoadProfileType.class);
        when(type.getId()).thenReturn(LOAD_PROFILE_TYPE_ID);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(type);
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfile.getId()).thenReturn(id);
        when(loadProfile.getLastReading()).thenReturn(LAST_READING);
        when(loadProfile.getDevice()).thenReturn(rtu);
        return loadProfile;
    }

    private LoadProfile getMockedLoadProfileWithTwoChannels(long loadProfileId, ObisCode loadProfileObisCode){
        LoadProfile newMockedLoadProfile = getNewMockedLoadProfile(loadProfileId, loadProfileObisCode);
        Channel channel1 = mock(Channel.class);
        Channel channel2 = mock(Channel.class);
        Channel channel3 = mock(Channel.class);
        Channel channel4 = mock(Channel.class);
        when(newMockedLoadProfile.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(newMockedLoadProfile.getAllChannels()).thenReturn(Arrays.asList(channel1, channel2, channel3, channel4));
        return newMockedLoadProfile;
    }

    @Test
    public void goOfflineTest() {
        final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
        LoadProfile loadProfile = getNewMockedLoadProfile(LOAD_PROFILE_ID, loadProfileObisCode);
        OfflineLoadProfileImpl offlineLoadProfile = new OfflineLoadProfileImpl(loadProfile);

        // Asserts
        assertNotNull(offlineLoadProfile);
        assertEquals("Expected the correct obiscode", loadProfileObisCode, offlineLoadProfile.getObisCode());
        assertEquals("Expected the correct Id of the LoadProfile", LOAD_PROFILE_ID, offlineLoadProfile.getLoadProfileId());
        assertEquals("Expected the correct profile Interval", PROFILE_INTERVAL, offlineLoadProfile.getInterval());
        assertEquals("Expected the correct lastReading", LAST_READING, offlineLoadProfile.getLastReading());
        assertEquals("Expected the correct Device ID", RTU_ID, offlineLoadProfile.getDeviceId());
        assertEquals("Expected the correct LoadProfileType ID", LOAD_PROFILE_TYPE_ID, offlineLoadProfile.getLoadProfileTypeId());
        assertEquals("Expected the correct Master SerialNumber", MASTER_SERIAL_NUMBER, offlineLoadProfile.getMasterSerialNumber());
    }

    //
    @Test
    public void convertToOfflineChannelsTest() {
        final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
        LoadProfile loadProfile = getMockedLoadProfileWithTwoChannels(LOAD_PROFILE_ID, loadProfileObisCode);
        OfflineLoadProfileImpl offlineLoadProfile = new OfflineLoadProfileImpl(loadProfile);

        // asserts
        assertNotNull(offlineLoadProfile.getChannels());
        assertEquals("Expected 5 channels of the master", 2, offlineLoadProfile.getChannels().size());
        assertNotNull(offlineLoadProfile.getAllChannels());
        assertEquals("Expected a total of 4 channels for this loadProfile", 4, offlineLoadProfile.getAllChannels().size());
    }
}
