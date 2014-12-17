package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import org.junit.Test;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierFirstOnDevice} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:55)
 */
public class LoadProfileIdentifierFirstOnDeviceTest {

    @Test(expected = NotFoundException.class)
    public void testDeviceDoesNotExist () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(NotFoundException.class).when(deviceIdentifier).findDevice();
        LoadProfileIdentifier<LoadProfile> loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier);

        // Business method
        loadProfileIdentifier.findLoadProfile();

        // Asserts: expected the NotFoundException reported by the DeviceIdentifier to be thrown or rethrown
    }

    @Test
    public void testWithDeviceWithoutLoadProfiles () {
        Device device = mock(Device.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        LoadProfileIdentifier<LoadProfile> loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier);

        // Business method
        LoadProfile loadProfile = loadProfileIdentifier.findLoadProfile();

        // Asserts
        assertThat(loadProfile).isNull();
    }

    @Test
    public void testWithDeviceWithOneLoadProfile () {
        LoadProfile expectedLoadProfile = mock(LoadProfile.class);
        Device device = mock(Device.class);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(expectedLoadProfile));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        LoadProfileIdentifier<LoadProfile> loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier);

        // Business method
        LoadProfile loadProfile = loadProfileIdentifier.findLoadProfile();

        // Asserts
        assertThat(loadProfile).isEqualTo(expectedLoadProfile);
    }

    @Test
    public void testWithDeviceWithMultipleLoadProfiles () {
        LoadProfile expectedLoadProfile = mock(LoadProfile.class);
        LoadProfile anotherLoadProfile = mock(LoadProfile.class);
        Device device = mock(Device.class);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(expectedLoadProfile, anotherLoadProfile));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        LoadProfileIdentifier<LoadProfile> loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier);

        // Business method
        LoadProfile loadProfile = loadProfileIdentifier.findLoadProfile();

        // Asserts
        assertThat(loadProfile).isEqualTo(expectedLoadProfile);
    }

}