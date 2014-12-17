package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.masterdata.LoadProfileType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link OfflineLoadProfileImpl} component
 *
 * @author gna
 * @since 30/05/12 - 14:40
 */
public class OfflineLoadProfileImplTest {

    private static final TimeDuration PROFILE_INTERVAL = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
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
        when(loadProfile.getLastReading()).thenReturn(Optional.of(LAST_READING.toInstant()));
        when(loadProfile.getDevice()).thenReturn(rtu);
        return loadProfile;
    }

    private LoadProfile getMockedLoadProfileWithTwoChannels(long loadProfileId, ObisCode loadProfileObisCode, TopologyService topologyService){
        LoadProfile newMockedLoadProfile = getNewMockedLoadProfile(loadProfileId, loadProfileObisCode);
        Channel channel1 = mock(Channel.class, RETURNS_DEEP_STUBS);
        Channel channel2 = mock(Channel.class, RETURNS_DEEP_STUBS);
        Channel channel3 = mock(Channel.class, RETURNS_DEEP_STUBS);
        Channel channel4 = mock(Channel.class, RETURNS_DEEP_STUBS);
        when(newMockedLoadProfile.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(topologyService.getAllChannels(newMockedLoadProfile)).thenReturn(Arrays.asList(channel1, channel2, channel3, channel4));
        return newMockedLoadProfile;
    }

    @Test
    public void goOfflineTest() {
        final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
        LoadProfile loadProfile = getNewMockedLoadProfile(LOAD_PROFILE_ID, loadProfileObisCode);
        OfflineLoadProfileImpl offlineLoadProfile = new OfflineLoadProfileImpl(loadProfile, mock(TopologyService.class), identificationService);

        // Asserts
        assertThat(offlineLoadProfile).isNotNull();
        assertThat(offlineLoadProfile.getObisCode()).isEqualTo(loadProfileObisCode);
        assertThat(offlineLoadProfile.getLoadProfileId()).isEqualTo(LOAD_PROFILE_ID);
        assertThat(offlineLoadProfile.getInterval()).isEqualTo(PROFILE_INTERVAL);
        assertThat(offlineLoadProfile.getLastReading()).isEqualTo(LAST_READING);
        assertThat(offlineLoadProfile.getDeviceId()).isEqualTo(RTU_ID);
        assertThat(offlineLoadProfile.getLoadProfileTypeId()).isEqualTo(LOAD_PROFILE_TYPE_ID);
        assertThat(offlineLoadProfile.getMasterSerialNumber()).isEqualTo(MASTER_SERIAL_NUMBER);
    }

    //
    @Test
    public void convertToOfflineChannelsTest() {
        final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
        TopologyService topologyService = mock(TopologyService.class);
        LoadProfile loadProfile = getMockedLoadProfileWithTwoChannels(LOAD_PROFILE_ID, loadProfileObisCode, topologyService);
        OfflineLoadProfileImpl offlineLoadProfile = new OfflineLoadProfileImpl(loadProfile, topologyService, identificationService);

        // asserts
        assertThat(offlineLoadProfile.getChannels()).isNotNull();
        assertThat(offlineLoadProfile.getChannels().size()).isEqualTo(2);
        assertThat(offlineLoadProfile.getAllChannels()).isNotNull();
        assertThat(offlineLoadProfile.getAllChannels().size()).isEqualTo(4);
    }

}