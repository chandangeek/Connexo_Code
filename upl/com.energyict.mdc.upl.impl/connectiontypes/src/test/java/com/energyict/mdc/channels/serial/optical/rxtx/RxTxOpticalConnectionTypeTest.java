package com.energyict.mdc.channels.serial.optical.rxtx;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link RxTxOpticalConnectionType} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:05
 */
public class RxTxOpticalConnectionTypeTest {

    @Test
    public void allowSimultaneousConnectionsTest() {
        RxTxOpticalConnectionType opticalConnectionType = new RxTxOpticalConnectionType();

        // assertion to test that no simultaneous connections are allowed on the serial connectionType
        assertFalse(opticalConnectionType.allowsSimultaneousConnections());
    }

    @Test
    public void getRequiredKeysTest() {
        RxTxOpticalConnectionType opticalConnectionType = new RxTxOpticalConnectionType();

        // asserts
        assertThat(opticalConnectionType.getRequiredProperties()).isNotEmpty();
        assertTrue(opticalConnectionType.isRequiredProperty(SerialPortConfiguration.PARITY_NAME));
        assertTrue(opticalConnectionType.isRequiredProperty(SerialPortConfiguration.BAUDRATE_NAME));
        assertTrue(opticalConnectionType.isRequiredProperty(SerialPortConfiguration.NR_OF_STOP_BITS_NAME));
        assertTrue(opticalConnectionType.isRequiredProperty(SerialPortConfiguration.NR_OF_DATA_BITS_NAME));
    }

    @Test
    public void getOptionalPropertiesTest() {
        RxTxOpticalConnectionType opticalConnectionType = new RxTxOpticalConnectionType();

        // asserts
        assertThat(opticalConnectionType.getOptionalProperties()).isNotEmpty();
        assertThat(opticalConnectionType.getOptionalProperties()).contains(opticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.FLOW_CONTROL_NAME));
    }

}
