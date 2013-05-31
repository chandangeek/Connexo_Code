package com.energyict.mdc.channels.serial.direct.serialio;

import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests for the {@link com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/08/12
 * Time: 13:17
 */
public class SioSerialConnectionTypeTest {

    @Test
    public void allowSimultaneousConnectionsTest() {
        SioSerialConnectionType sioSerialConnectionType = new SioSerialConnectionType();

        assertFalse(sioSerialConnectionType.allowsSimultaneousConnections());
    }

    @Test
    public void getRequiredKeysTest() {
        SioSerialConnectionType serialConnectionType = new SioSerialConnectionType();

        // asserts
        assertThat(serialConnectionType.getRequiredProperties()).isNotEmpty();
        assertTrue(serialConnectionType.isRequiredProperty(SerialPortConfiguration.PARITY_NAME));
        assertTrue(serialConnectionType.isRequiredProperty(SerialPortConfiguration.BAUDRATE_NAME));
        assertTrue(serialConnectionType.isRequiredProperty(SerialPortConfiguration.NR_OF_STOP_BITS_NAME));
        assertTrue(serialConnectionType.isRequiredProperty(SerialPortConfiguration.NR_OF_DATA_BITS_NAME));
    }

    @Test
    public void getOptionalPropertiesTest(){
        SioSerialConnectionType serialConnectionType = new SioSerialConnectionType();

        // asserts
        assertThat(serialConnectionType.getOptionalProperties()).isNotEmpty();
        assertThat(serialConnectionType.getOptionalProperties()).contains(serialConnectionType.getPropertySpec(SerialPortConfiguration.FLOW_CONTROL_NAME));
        assertThat(serialConnectionType.getOptionalProperties()).contains(serialConnectionType.getPropertySpec(SerialPortConfiguration.SERIAL_PORT_READ_TIMEOUT_NAME));
        assertThat(serialConnectionType.getOptionalProperties()).contains(serialConnectionType.getPropertySpec(SerialPortConfiguration.SERIAL_PORT_WRITE_TIMEOUT_NAME));
    }
}
