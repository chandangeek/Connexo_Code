package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

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

    @Test
    public void testConstructorExtractsInformation () {
        // Business method
        new UnknownSlaveDeviceEvent(this.masterDeviceIdentifier, this.deviceIdentifier);

        // Asserts
        verify(this.masterDeviceIdentifier).toString();
        verify(this.deviceIdentifier).toString();
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