package com.energyict.mdc.channels.serial.optical.serialio;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link SioOpticalConnectionType} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:03
 */
public class SioOpticalConnectionTypeTest {


    @Test
    public void allowSimultaneousConnectionsTest() {
        SioOpticalConnectionType sioOpticalConnectionType = new SioOpticalConnectionType();

        assertFalse(sioOpticalConnectionType.allowsSimultaneousConnections());
    }

    @Test
    public void getRequiredKeysTest() {
        SioOpticalConnectionType sioOpticalConnectionType = new SioOpticalConnectionType();

        // asserts
        assertThat(sioOpticalConnectionType.getRequiredProperties()).isNotEmpty();
        assertTrue(sioOpticalConnectionType.isRequiredProperty(SerialPortConfiguration.PARITY_NAME));
        assertTrue(sioOpticalConnectionType.isRequiredProperty(SerialPortConfiguration.BAUDRATE_NAME));
        assertTrue(sioOpticalConnectionType.isRequiredProperty(SerialPortConfiguration.NR_OF_STOP_BITS_NAME));
        assertTrue(sioOpticalConnectionType.isRequiredProperty(SerialPortConfiguration.NR_OF_DATA_BITS_NAME));
    }

    @Test
    public void getOptionalPropertiesTest() {
        SioOpticalConnectionType sioOpticalConnectionType = new SioOpticalConnectionType();

        // asserts
        assertThat(sioOpticalConnectionType.getOptionalProperties()).isNotEmpty();
        assertThat(sioOpticalConnectionType.getOptionalProperties()).contains(sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.FLOW_CONTROL_NAME));
    }

}
