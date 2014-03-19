package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import org.junit.*;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link FirstLoadProfileOnDevice} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:55)
 */
public class FirstLoadProfileOnDeviceTest extends AbstractEIWebTests{

    @Test(expected = NotFoundException.class)
    public void testDeviceDoesNotExist () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(NotFoundException.class).when(deviceIdentifier).findDevice();
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier);

        // Business method
        loadProfileIdentifier.findLoadProfile();

        // Asserts: expected the NotFoundException reported by the DeviceIdentifier to be thrown or rethrown
    }

    @Test
    public void testWithDeviceWithoutLoadProfiles () {
        BaseDevice device = mock(BaseDevice.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier);

        // Business method
        BaseLoadProfile loadProfile = loadProfileIdentifier.findLoadProfile();

        // Asserts
        assertThat(loadProfile).isNull();
    }

    @Test
    public void testWithDeviceWithOneLoadProfile () {
        BaseLoadProfile expectedLoadProfile = mock(BaseLoadProfile.class);
        BaseDevice device = mock(BaseDevice.class);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(expectedLoadProfile));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier);

        // Business method
        BaseLoadProfile loadProfile = loadProfileIdentifier.findLoadProfile();

        // Asserts
        assertThat(loadProfile).isEqualTo(expectedLoadProfile);
    }

    @Test
    public void testWithDeviceWithMultipleLoadProfiles () {
        BaseLoadProfile expectedLoadProfile = mock(BaseLoadProfile.class);
        BaseLoadProfile anotherLoadProfile = mock(BaseLoadProfile.class);
        BaseDevice device = mock(BaseDevice.class);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(expectedLoadProfile, anotherLoadProfile));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier);

        // Business method
        BaseLoadProfile loadProfile = loadProfileIdentifier.findLoadProfile();

        // Asserts
        assertThat(loadProfile).isEqualTo(expectedLoadProfile);
    }

}