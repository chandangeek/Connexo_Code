package com.energyict.mdc.channels.serial.direct.rxtx;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType} component
 *
 * Copyrights EnergyICT
 * Date: 13/08/12
 * Time: 15:21
 */
public class RxTxSerialConnectionTypeTest {

    @Test
    public void allowSimultaneousConnectionsTest(){
        RxTxSerialConnectionType serialConnectionType = new RxTxSerialConnectionType();

        // assertion to test that no simultaneous connections are allowed on the serial connectionType
        assertFalse(serialConnectionType.allowsSimultaneousConnections());
    }

    @Test
    public void getRequiredKeysTest(){
        RxTxSerialConnectionType serialConnectionType = new RxTxSerialConnectionType();

        // asserts
        assertThat(serialConnectionType.getRequiredProperties()).isNotEmpty();
        assertTrue(serialConnectionType.isRequiredProperty(SerialPortConfiguration.PARITY_NAME));
        assertTrue(serialConnectionType.isRequiredProperty(SerialPortConfiguration.BAUDRATE_NAME));
        assertTrue(serialConnectionType.isRequiredProperty(SerialPortConfiguration.NR_OF_STOP_BITS_NAME));
        assertTrue(serialConnectionType.isRequiredProperty(SerialPortConfiguration.NR_OF_DATA_BITS_NAME));
    }

    @Test
    public void getOptionalPropertiesTest(){
        RxTxSerialConnectionType serialConnectionType = new RxTxSerialConnectionType();

        // asserts
        assertThat(serialConnectionType.getOptionalProperties()).isNotEmpty();
        assertThat(serialConnectionType.getOptionalProperties()).contains(serialConnectionType.getUPLPropertySpec(SerialPortConfiguration.FLOW_CONTROL_NAME));
    }
}
