package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LoadProfileDataIdentifier}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 16/10/12
 * Time: 11:27
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadProfileDataIdentifierTest {

    private static ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");

    @Mock
    private Device device;

    private DeviceIdentifier getMockedDeviceIdentifier() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(this.device);
        return deviceIdentifier;
    }

    @Test(expected = NotFoundException.class)
    public void loadProfileDoesNotExist() {
        when(this.device.getLoadProfiles()).thenReturn(new ArrayList<LoadProfile>(0));

        // business method
        new LoadProfileDataIdentifier(loadProfileObisCode, getMockedDeviceIdentifier()).findLoadProfile();
    }

    @Test(expected = NotFoundException.class)
    public void deviceDoesNotExist() {
        final DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(null);

        // Business method
        new LoadProfileDataIdentifier(loadProfileObisCode, deviceIdentifier).findLoadProfile();
    }

    @Test
    public void singleLoadProfileTest(){
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getDeviceObisCode()).thenReturn(loadProfileObisCode);
        LoadProfile singleLoadProfile = mock(LoadProfile.class);
        when(singleLoadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(singleLoadProfile));

        // business method
        BaseLoadProfile loadProfile = new LoadProfileDataIdentifier(loadProfileObisCode, getMockedDeviceIdentifier()).findLoadProfile();

        // Asserts
        assertThat(loadProfile).isNotNull();
        assertThat(loadProfile).isEqualTo(singleLoadProfile);
    }

    @Test
    public void multipleLoadProfilesTest(){
        LoadProfileSpec loadProfileSpec1 = mock(LoadProfileSpec.class);
        when(loadProfileSpec1.getDeviceObisCode()).thenReturn(loadProfileObisCode);
        LoadProfile mLoadProfile1 = mock(LoadProfile.class);
        when(mLoadProfile1.getLoadProfileSpec()).thenReturn(loadProfileSpec1);
        LoadProfileSpec loadProfileSpec2 = mock(LoadProfileSpec.class);
        when(loadProfileSpec2.getDeviceObisCode()).thenReturn(ObisCode.fromString("0.0.0.0.0.0"));
        LoadProfile mLoadProfile2 = mock(LoadProfile.class);
        when(mLoadProfile2.getLoadProfileSpec()).thenReturn(loadProfileSpec2);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(mLoadProfile1, mLoadProfile2));

        // business method
        BaseLoadProfile loadProfile = new LoadProfileDataIdentifier(loadProfileObisCode, getMockedDeviceIdentifier()).findLoadProfile();

        // Asserts
        assertThat(loadProfile).isNotNull();
        assertThat(loadProfile).isEqualTo(mLoadProfile1);
    }
}
