package com.energyict.protocols.impl.channels.serial.direct.serialio;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.impl.channels.ConnectionTypePropertiesTest;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import org.junit.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;


/**
 * Tests the {@link SioSerialConnectionType} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/08/12
 * Time: 13:17
 */
public class SioSerialConnectionTypeTest extends ConnectionTypePropertiesTest {

    @Test
    public void allowSimultaneousConnectionsTest() {
        SioSerialConnectionType sioSerialConnectionType = newConnectionType();

        assertFalse(sioSerialConnectionType.allowsSimultaneousConnections());
    }

    protected SioSerialConnectionType newConnectionType () {
        return new SioSerialConnectionType();
    }

    @Override
    protected Set<String> requiredPropertyNames () {
        return new HashSet<>(Arrays.asList(
                SerialPortConfiguration.PARITY_NAME,
                SerialPortConfiguration.BAUDRATE_NAME,
                SerialPortConfiguration.NR_OF_STOP_BITS_NAME,
                SerialPortConfiguration.NR_OF_DATA_BITS_NAME));
    }

    @Override
    protected Set<String> optionalPropertyNames () {
        return new HashSet<>(Arrays.asList(SerialPortConfiguration.FLOW_CONTROL_NAME));
    }

    @Test
    public void getPossibleValuesInCorrectOrderTest() {
        SioSerialConnectionType serialConnectionType = newConnectionType();

        PropertySpec baudrate = serialConnectionType.getPropertySpec(SerialPortConfiguration.BAUDRATE_NAME);

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