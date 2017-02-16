package com.energyict.mdc.channels.serial.direct.serialio;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import org.junit.Test;

import java.math.BigDecimal;

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
        assertThat(serialConnectionType.getOptionalProperties()).contains(serialConnectionType.getUPLPropertySpec(SerialPortConfiguration.FLOW_CONTROL_NAME));
    }

    @Test
    public void getPossibleValuesInCorrectOrderTest() {
        SioSerialConnectionType serialConnectionType = new SioSerialConnectionType();

        PropertySpec baudrate = serialConnectionType.getUPLPropertySpec(SerialPortConfiguration.BAUDRATE_NAME);

        assertThat(baudrate.getPossibleValues().getAllValues().get(0)).isEqualTo(new BigDecimal(150));
        assertThat(baudrate.getPossibleValues().getAllValues().get(1)).isEqualTo(new BigDecimal(300));
        assertThat(baudrate.getPossibleValues().getAllValues().get(2)).isEqualTo(new BigDecimal(600));
        assertThat(baudrate.getPossibleValues().getAllValues().get(3)).isEqualTo(new BigDecimal(1200));
        assertThat(baudrate.getPossibleValues().getAllValues().get(4)).isEqualTo(new BigDecimal(2400));
        assertThat(baudrate.getPossibleValues().getAllValues().get(5)).isEqualTo(new BigDecimal(4800));
        assertThat(baudrate.getPossibleValues().getAllValues().get(6)).isEqualTo(new BigDecimal(9600));
        assertThat(baudrate.getPossibleValues().getAllValues().get(7)).isEqualTo(new BigDecimal(19200));
        assertThat(baudrate.getPossibleValues().getAllValues().get(8)).isEqualTo(new BigDecimal(38400));
        assertThat(baudrate.getPossibleValues().getAllValues().get(9)).isEqualTo(new BigDecimal(57600));
        assertThat(baudrate.getPossibleValues().getAllValues().get(10)).isEqualTo(new BigDecimal(115200));
        assertThat(baudrate.getPossibleValues().getAllValues().get(11)).isEqualTo(new BigDecimal(230400));
        assertThat(baudrate.getPossibleValues().getAllValues().get(12)).isEqualTo(new BigDecimal(460800));
    }
}
