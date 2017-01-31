/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Ignore //TODO GOVANNI NEEDS TO FIX THEM
@RunWith(MockitoJUnitRunner.class)
public class LoadProfileIdentifierForAlreadyKnownLoadProfileTest {

    @Mock
    private Device device;
    @Mock
    private LoadProfile loadProfile;

    @Before
    public void setup() {
        when(loadProfile.getDevice()).thenReturn(device);
    }

    @Ignore //TODO GOVANNI NEEDS TO FIX THEM
    @Test
    public void serialNumberIdentifierShouldBeUsedTest() {
        LoadProfileIdentifierForAlreadyKnownLoadProfile loadProfileIdentifierForAlreadyKnownLoadProfile = new LoadProfileIdentifierForAlreadyKnownLoadProfile(loadProfile, ObisCode.fromString("1.1.1.1.1.1"));

        assertThat(loadProfileIdentifierForAlreadyKnownLoadProfile.getDeviceIdentifier().getDeviceIdentifierType()).isEqualTo(DeviceIdentifierType.SerialNumber);
    }
}