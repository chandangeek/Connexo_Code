package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.protocol.exceptions.identifier.NotFoundException;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link FirstLoadProfileOnDevice} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:55)
 */
public class FirstLoadProfileOnDeviceTest {

    @Test(expected = NotFoundException.class)
    public void testDeviceDoesNotExist () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(NotFoundException.class).when(deviceIdentifier).findDevice();
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier, DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE);

        // Business method
        loadProfileIdentifier.getLoadProfile();

        // Asserts: expected the NotFoundException reported by the DeviceIdentifier to be thrown or rethrown
    }

    @Test(expected = NotFoundException.class)
    public void testWithDeviceWithoutLoadProfiles () {
        Device device = mock(Device.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier, DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE);

        // Business method
        LoadProfile loadProfile = loadProfileIdentifier.getLoadProfile();
    }

    @Test
    public void testIdentifierToString() {
        LoadProfile expectedLoadProfile = mock(LoadProfile.class);
        Device device = mock(Device.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("id 1");
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier, DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE);

        // Business method
        String toStringMessage = loadProfileIdentifier.toString();

        // Asserts
        assertEquals("first load profile on device having deviceIdentifier = id 1", toStringMessage);
    }


    @Test
    public void testWithDeviceWithOneLoadProfile () {
        LoadProfile expectedLoadProfile = mock(LoadProfile.class);
        Device device = mock(Device.class);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(expectedLoadProfile));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier, DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE);

        // Business method
        LoadProfile loadProfile = loadProfileIdentifier.getLoadProfile();

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
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier, DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE);

        // Business method
        LoadProfile loadProfile = loadProfileIdentifier.getLoadProfile();

        // Asserts
        assertThat(loadProfile).isEqualTo(expectedLoadProfile);
    }

}