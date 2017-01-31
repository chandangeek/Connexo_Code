/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

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
public class DeviceMessageIdentifierForAlreadyKnownMessageTest {

    @Mock
    private Device device;
    @Mock
    private DeviceMessage<Device> deviceMessage;

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