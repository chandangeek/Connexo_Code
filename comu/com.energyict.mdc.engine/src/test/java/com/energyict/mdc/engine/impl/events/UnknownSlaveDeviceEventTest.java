/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link UnknownSlaveDeviceEvent} component.
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (14:33)
 */
@RunWith(MockitoJUnitRunner.class)
public class UnknownSlaveDeviceEventTest {

    private static final String MASTER_DEVICE_IDENTIFIER = "UnknownSlaveDeviceEventTest.master";
    private static final String DEVICE_IDENTIFIER = "UnknownSlaveDeviceEventTest.slave";

    @Mock
    private DeviceIdentifier masterDeviceIdentifier;
    @Mock
    private DeviceIdentifier deviceIdentifier;

    @Before
    public void initializeMocks () {
        when(this.masterDeviceIdentifier.getIdentifier()).thenReturn(MASTER_DEVICE_IDENTIFIER);
        when(this.deviceIdentifier.getIdentifier()).thenReturn(DEVICE_IDENTIFIER);
    }

    @Test
    public void testConstructorExtractsInformation () {
        // Business method
        new UnknownSlaveDeviceEvent(this.masterDeviceIdentifier, this.deviceIdentifier);

        // Asserts
        verify(this.masterDeviceIdentifier).getIdentifier();
        verify(this.deviceIdentifier).getIdentifier();
    }

    @Test
    public void testGetMasterDeviceIdentifier () {
        // Business method
        UnknownSlaveDeviceEvent unknownSlaveDeviceEvent = new UnknownSlaveDeviceEvent(this.masterDeviceIdentifier, this.deviceIdentifier);

        // Asserts
        assertThat(unknownSlaveDeviceEvent.getMasterDeviceId()).isEqualTo(MASTER_DEVICE_IDENTIFIER);
    }

    @Test
    public void testGetDeviceIdentifier () {
        // Business method
        UnknownSlaveDeviceEvent unknownSlaveDeviceEvent = new UnknownSlaveDeviceEvent(this.masterDeviceIdentifier, this.deviceIdentifier);

        // Asserts
        assertThat(unknownSlaveDeviceEvent.getDeviceIdentifier()).isEqualTo(DEVICE_IDENTIFIER);
    }

}