package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.LoadProfileServiceImpl;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.obis.ObisCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LoadProfileIdentifierFirstOnDevice} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:55)
 */
public class LoadProfileIdentifierFirstOnDeviceTest {

    @Mock
    private ServerDeviceService deviceService;
    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private LoadProfileService loadProfileService;

    @Before
    public void setup() {
        when(deviceDataModelService.deviceService()).thenReturn(deviceService);
        loadProfileService = new LoadProfileServiceImpl(deviceDataModelService);
    }

    @Test
    public void testDeviceDoesNotExist() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doReturn(Optional.empty()).when(deviceService).findDeviceByIdentifier(deviceIdentifier);
        LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier, ObisCode.fromString("1.1.1.1.1.1"));

        // Business method
        Optional<LoadProfile> loadProfile = loadProfileService.findByIdentifier(loadProfileIdentifier);

        assertTrue(!loadProfile.isPresent());
    }

    @Test()
    public void testWithDeviceWithoutLoadProfiles() {
        Device device = mock(Device.class);
        when(device.getLoadProfiles()).thenReturn(Collections.emptyList());
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doReturn(device).when(deviceService).findDeviceByIdentifier(deviceIdentifier);

        LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier, ObisCode.fromString("1.1.1.1.1.1"));

        // Business method
        Optional<LoadProfile> loadProfile = loadProfileService.findByIdentifier(loadProfileIdentifier);
        assertTrue(!loadProfile.isPresent());
    }

    @Test
    public void testWithDeviceWithOneLoadProfile() {
        LoadProfile expectedLoadProfile = mock(LoadProfile.class);
        Device device = mock(Device.class);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(expectedLoadProfile));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doReturn(device).when(deviceService).findDeviceByIdentifier(deviceIdentifier);

        LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier, ObisCode.fromString("1.1.1.1.1.1"));

        // Business method
        Optional<LoadProfile> loadProfile = loadProfileService.findByIdentifier(loadProfileIdentifier);

        // Asserts
        assertTrue(loadProfile.isPresent());
        assertThat(loadProfile.get()).isEqualTo(expectedLoadProfile);
    }

    @Test
    public void testWithDeviceWithMultipleLoadProfiles() {
        LoadProfile expectedLoadProfile = mock(LoadProfile.class);
        LoadProfile anotherLoadProfile = mock(LoadProfile.class);
        Device device = mock(Device.class);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(expectedLoadProfile, anotherLoadProfile));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doReturn(device).when(deviceService).findDeviceByIdentifier(deviceIdentifier);

        LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier, ObisCode.fromString("1.1.1.1.1.1"));

        // Business method
        Optional<LoadProfile> loadProfile = loadProfileService.findByIdentifier(loadProfileIdentifier);

        // Asserts
        assertTrue(loadProfile.isPresent());
        assertThat(loadProfile).isEqualTo(expectedLoadProfile);
    }

}