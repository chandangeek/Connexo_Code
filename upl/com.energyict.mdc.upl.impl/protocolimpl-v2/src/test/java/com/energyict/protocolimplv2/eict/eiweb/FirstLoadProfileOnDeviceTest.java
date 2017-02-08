package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.protocol.exception.identifier.NotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
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
@RunWith(MockitoJUnitRunner.class)
public class FirstLoadProfileOnDeviceTest {

    @Mock
    private DeviceFactory deviceFactory;

    @Before
    public void setDeviceFactory() {
        DeviceFactoryProvider.instance.set(() -> deviceFactory);
    }

    @After
    public void clearDeviceFactory() {
        DeviceFactoryProvider.instance.set(() -> null);
    }

    @Test(expected = NotFoundException.class)
    public void testDeviceDoesNotExist () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(NotFoundException.class).when(this.deviceFactory).find(deviceIdentifier);
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier, DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE);

        // Business method
        loadProfileIdentifier.getLoadProfile();

        // Asserts: expected the NotFoundException reported by the DeviceIdentifier to be thrown or rethrown
    }

    @Test(expected = NotFoundException.class)
    public void testWithDeviceWithoutLoadProfiles () {
        Device device = mock(Device.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceFactory.find(deviceIdentifier)).thenReturn(device);
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
        when(this.deviceFactory.find(deviceIdentifier)).thenReturn(device);
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
        when(this.deviceFactory.find(deviceIdentifier)).thenReturn(device);
        LoadProfileIdentifier loadProfileIdentifier = new FirstLoadProfileOnDevice(deviceIdentifier, DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE);

        // Business method
        LoadProfile loadProfile = loadProfileIdentifier.getLoadProfile();

        // Asserts
        assertThat(loadProfile).isEqualTo(expectedLoadProfile);
    }

}