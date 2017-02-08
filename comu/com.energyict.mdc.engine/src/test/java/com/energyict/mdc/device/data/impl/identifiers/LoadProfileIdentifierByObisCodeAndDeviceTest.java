/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadProfileIdentifierByObisCodeAndDeviceTest {

    private static ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");

    @Mock
    private Device device;

    private DeviceIdentifier getMockedDeviceIdentifier() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(this.device);
        return deviceIdentifier;
    }

    @Test(expected = CanNotFindForIdentifier.class)
    public void loadProfileDoesNotExist() {
        when(this.device.getLoadProfiles()).thenReturn(new ArrayList<>(0));

        // business method
        new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, getMockedDeviceIdentifier()).findLoadProfile();
    }

    @Test(expected = CanNotFindForIdentifier.class)
    public void deviceDoesNotExist() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(CanNotFindForIdentifier.class).when(deviceIdentifier).findDevice();

        // Business method
        new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, deviceIdentifier).findLoadProfile();
    }

    @Test
    public void singleLoadProfileTest(){
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getDeviceObisCode()).thenReturn(loadProfileObisCode);
        LoadProfile singleLoadProfile = mock(LoadProfile.class);
        when(singleLoadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        when(singleLoadProfile.getDeviceObisCode()).thenReturn(loadProfileObisCode);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(singleLoadProfile));

        // business method
        LoadProfile loadProfile = new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, getMockedDeviceIdentifier()).findLoadProfile();

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
        when(mLoadProfile1.getDeviceObisCode()).thenReturn(loadProfileObisCode);
        LoadProfileSpec loadProfileSpec2 = mock(LoadProfileSpec.class);
        ObisCode loadProfileSpec2ObisCode = ObisCode.fromString("0.0.0.0.0.0");
        when(loadProfileSpec2.getDeviceObisCode()).thenReturn(loadProfileSpec2ObisCode);
        LoadProfile mLoadProfile2 = mock(LoadProfile.class);
        when(mLoadProfile2.getLoadProfileSpec()).thenReturn(loadProfileSpec2);
        when(mLoadProfile2.getDeviceObisCode()).thenReturn(loadProfileSpec2ObisCode);
        when(this.device.getLoadProfiles()).thenReturn(Arrays.asList(mLoadProfile1, mLoadProfile2));

        // business method
        LoadProfile loadProfile = new LoadProfileIdentifierByObisCodeAndDevice(loadProfileObisCode, getMockedDeviceIdentifier()).findLoadProfile();

        // Asserts
        assertThat(loadProfile).isNotNull();
        assertThat(loadProfile).isEqualTo(mLoadProfile1);
    }
}
