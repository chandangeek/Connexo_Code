package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.obis.ObisCode;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LoadProfileIdentifierFirstOnDevice} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:55)
 */
public class LoadProfileIdentifierFirstOnDeviceTest {

    @Test(expected = NotFoundException.class)
    public void testDeviceDoesNotExist () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(NotFoundException.class).when(deviceIdentifier).findDevice();
        LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier, ObisCode.fromString("1.1.1.1.1.1"));

        // Business method
        loadProfileIdentifier.getLoadProfile();

        // Asserts: expected the NotFoundException reported by the DeviceIdentifier to be thrown or rethrown
    }

    @Test(expected = CanNotFindForIdentifier.class)
    public void testWithDeviceWithoutLoadProfiles () {
        Device device = mock(Device.class);
        when(device.getLoadProfiles()).thenReturn(Collections.emptyList());
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier, ObisCode.fromString("1.1.1.1.1.1"));

        // Business method
        LoadProfile loadProfile = (com.energyict.mdc.device.data.LoadProfile) loadProfileIdentifier.getLoadProfile();

        // Asserts: see expected exception rule
    }

    @Test
    public void testWithDeviceWithOneLoadProfile () {
        LoadProfile expectedLoadProfile = mock(LoadProfile.class);
        Device device = mock(Device.class);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(expectedLoadProfile));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier, ObisCode.fromString("1.1.1.1.1.1"));

        // Business method
        LoadProfile loadProfile = (com.energyict.mdc.device.data.LoadProfile) loadProfileIdentifier.getLoadProfile();

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
        LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(deviceIdentifier, ObisCode.fromString("1.1.1.1.1.1"));

        // Business method
        LoadProfile loadProfile = (com.energyict.mdc.device.data.LoadProfile) loadProfileIdentifier.getLoadProfile();

        // Asserts
        assertThat(loadProfile).isEqualTo(expectedLoadProfile);
    }

}