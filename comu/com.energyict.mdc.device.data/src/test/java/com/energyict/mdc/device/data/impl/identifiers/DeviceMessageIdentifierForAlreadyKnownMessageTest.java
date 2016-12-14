package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifierType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 06/05/15
 * Time: 10:14
 */
@Ignore //TODO GOVANNI NEEDS TO FIX THEM
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageIdentifierForAlreadyKnownMessageTest {

    @Mock
    private Device device;
    @Mock
    private DeviceMessage deviceMessage;

    @Before
    public void setup() {
        when(deviceMessage.getDevice()).thenReturn(device);
    }

    @Ignore //TODO GOVANNI NEEDS TO FIX THEM
    @Test
    public void serialNumberIdentifierShouldBeUsedTest() {
        DeviceMessageIdentifierForAlreadyKnownMessage deviceMessageIdentifierForAlreadyKnownMessage = new DeviceMessageIdentifierForAlreadyKnownMessage(deviceMessage);

        assertThat(deviceMessageIdentifierForAlreadyKnownMessage.getDeviceIdentifier().getDeviceIdentifierType()).isEqualTo(DeviceIdentifierType.SerialNumber);
    }
}