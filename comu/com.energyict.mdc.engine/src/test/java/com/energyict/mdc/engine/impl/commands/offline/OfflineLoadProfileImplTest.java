/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierForAlreadyKnownDeviceBySerialNumber;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link OfflineLoadProfileImpl} component
 *
 * @author gna
 * @since 30/05/12 - 14:40
 */
@RunWith(MockitoJUnitRunner.class)
public class OfflineLoadProfileImplTest {

    private static final TimeDuration PROFILE_INTERVAL = new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
    private static final Instant LAST_READING = Instant.ofEpochMilli(1338381863000L);
    private static final long RTU_ID = 4565;
    private static final int LOAD_PROFILE_ID = 48564;
    private static final long LOAD_PROFILE_TYPE_ID = 11565;
    private static final String MASTER_SERIAL_NUMBER = "Master_SerialNumber";
    @Mock
    private IdentificationService identificationService;
    @Mock
    private TopologyService topologyService;

    private LoadProfile getNewMockedLoadProfile(final long id, final ObisCode obisCode) {
        Device device = getMockedDevice();
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getDeviceObisCode()).thenReturn(obisCode);
        when(loadProfileSpec.getInterval()).thenReturn(PROFILE_INTERVAL);
        LoadProfileType type = mock(LoadProfileType.class);
        when(type.getId()).thenReturn(LOAD_PROFILE_TYPE_ID);
        when(loadProfileSpec.getLoadProfileType()).thenReturn(type);
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(loadProfile.getId()).thenReturn(id);
        when(loadProfile.getLastReading()).thenReturn(Optional.of(LAST_READING));
        when(loadProfile.getDevice()).thenReturn(device);
        return loadProfile;
    }

    private Device getMockedDevice() {
        Device device = mock(Device.class);
        when(device.getSerialNumber()).thenReturn(MASTER_SERIAL_NUMBER);
        when(device.getId()).thenReturn(RTU_ID);
        return device;
    }

    private LoadProfile getMockedLoadProfileWithTwoChannels(long loadProfileId, ObisCode loadProfileObisCode, TopologyService topologyService) {
        LoadProfile newMockedLoadProfile = getNewMockedLoadProfile(loadProfileId, loadProfileObisCode);
        Channel channel1 = mockChannel(newMockedLoadProfile);
        Channel channel2 = mockChannel(newMockedLoadProfile);
        Channel channel3 = mockChannel(newMockedLoadProfile);
        Channel channel4 = mockChannel(newMockedLoadProfile);
        Device device1 = mock(Device.class);
        when(device1.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        Device device2 = mock(Device.class);
        when(device2.getChannels()).thenReturn(Arrays.asList(channel3, channel4));
        when(device2.isLogicalSlave()).thenReturn(true);
        when(newMockedLoadProfile.getDevice()).thenReturn(device1);
        when(topologyService.findPhysicalConnectedDevices(device1)).thenReturn(Arrays.asList(device2));
        when(newMockedLoadProfile.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        return newMockedLoadProfile;
    }

    private Channel mockChannel(LoadProfile loadProfile) {
        Channel channel = mock(Channel.class, RETURNS_DEEP_STUBS);
        when(channel.getOverflow()).thenReturn(Optional.empty());
        when(channel.getLoadProfile()).thenReturn(loadProfile);
        return channel;
    }

    @Test
    public void goOfflineTest() {
        final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
        LoadProfile loadProfile = getNewMockedLoadProfile(LOAD_PROFILE_ID, loadProfileObisCode);
        OfflineLoadProfileImpl offlineLoadProfile = new OfflineLoadProfileImpl(loadProfile, topologyService, this.identificationService);

        // Asserts
        assertThat(offlineLoadProfile).isNotNull();
        assertThat(offlineLoadProfile.getObisCode()).isEqualTo(loadProfileObisCode);
        assertThat(offlineLoadProfile.getLoadProfileId()).isEqualTo(LOAD_PROFILE_ID);
        assertThat(offlineLoadProfile.getInterval()).isEqualTo(PROFILE_INTERVAL);
        assertThat(offlineLoadProfile.getLastReading().isPresent()).isTrue();
        assertThat(offlineLoadProfile.getLastReading().get()).isEqualTo(LAST_READING);
        assertThat(offlineLoadProfile.getDeviceId()).isEqualTo(RTU_ID);
        assertThat(offlineLoadProfile.getLoadProfileTypeId()).isEqualTo(LOAD_PROFILE_TYPE_ID);
        assertThat(offlineLoadProfile.getMasterSerialNumber()).isEqualTo(MASTER_SERIAL_NUMBER);
    }

    //
    @Test
    public void convertToOfflineChannelsTest() {
        final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
        LoadProfile loadProfile = getMockedLoadProfileWithTwoChannels(LOAD_PROFILE_ID, loadProfileObisCode, topologyService);
        OfflineLoadProfileImpl offlineLoadProfile = new OfflineLoadProfileImpl(loadProfile, topologyService, identificationService);

        // asserts
        assertThat(offlineLoadProfile.getChannels()).isNotNull();
        assertThat(offlineLoadProfile.getChannels().size()).isEqualTo(2);
        assertThat(offlineLoadProfile.getAllChannels()).isNotNull();
        assertThat(offlineLoadProfile.getAllChannels().size()).isEqualTo(4);
    }

    @Test
    public void deviceIdentifierForKnownDeviceBySerialNumberShouldBeUsedTest() {
        final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
        LoadProfile loadProfile = getNewMockedLoadProfile(LOAD_PROFILE_ID, loadProfileObisCode);
        Device device = getMockedDevice();
        DeviceIdentifierForAlreadyKnownDeviceBySerialNumber deviceIdentifier = new DeviceIdentifierForAlreadyKnownDeviceBySerialNumber(device);
        when(identificationService.createDeviceIdentifierForAlreadyKnownDevice(any(BaseDevice.class))).thenReturn(deviceIdentifier);

        OfflineLoadProfileImpl offlineLoadProfile = new OfflineLoadProfileImpl(loadProfile, topologyService, identificationService);

        assertThat(offlineLoadProfile.getDeviceIdentifier().getDeviceIdentifierType()).isEqualTo(DeviceIdentifierType.SerialNumber);
    }


}