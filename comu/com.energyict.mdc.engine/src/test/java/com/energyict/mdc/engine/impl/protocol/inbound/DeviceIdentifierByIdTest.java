/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.protocol.inbound;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceIdentifierById} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (11:19)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceIdentifierByIdTest {

    private static final long DEVICE_ID = 97;

    @Mock
    private DeviceService deviceService;

    @Test(expected = CanNotFindForIdentifier.class)
    public void testDeviceDoesNotExist() {
        when(this.deviceService.findDeviceById(DEVICE_ID)).thenReturn(Optional.<Device>empty());

        // Business method
        new DeviceIdentifierById(DEVICE_ID, this.deviceService).findDevice();

        // Expected a CanNotFindForIdentifier
    }

    @Test
    public void testOnlyOneDevice() {
        Device device = mock(Device.class);
        when(this.deviceService.findDeviceById(DEVICE_ID)).thenReturn(Optional.of(device));

        // Business method
        BaseDevice foundDevice = new DeviceIdentifierById(DEVICE_ID, this.deviceService).findDevice();

        // Asserts
        assertThat(foundDevice).isEqualTo(device);
    }

}