package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 06/05/15
 * Time: 10:11
 */
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

    @Test
    public void serialNumberIdentifierShouldBeUsedTest() {
        LoadProfileIdentifierForAlreadyKnownLoadProfile loadProfileIdentifierForAlreadyKnownLoadProfile = new LoadProfileIdentifierForAlreadyKnownLoadProfile(loadProfile);

        assertThat(loadProfileIdentifierForAlreadyKnownLoadProfile.getDeviceIdentifier().getDeviceIdentifierType()).isEqualTo(DeviceIdentifierType.SerialNumber);
    }
}