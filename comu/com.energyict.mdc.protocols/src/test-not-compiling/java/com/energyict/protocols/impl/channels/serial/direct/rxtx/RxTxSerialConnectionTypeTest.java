package com.energyict.protocols.impl.channels.serial.direct.rxtx;

import com.energyict.mdc.channels.serial.SerialPortConfiguration;

import com.energyict.protocols.impl.channels.ConnectionTypePropertiesTest;
import org.junit.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests the {@link RxTxSerialConnectionType} component.
 *
 * Copyrights EnergyICT
 * Date: 13/08/12
 * Time: 15:21
 */
public class RxTxSerialConnectionTypeTest extends ConnectionTypePropertiesTest {

    @Test
    public void allowSimultaneousConnectionsTest(){
        RxTxSerialConnectionType serialConnectionType = newConnectionType();

        // assertion to test that no simultaneous connections are allowed on the serial connectionType
        assertFalse(serialConnectionType.allowsSimultaneousConnections());
    }

    @Override
    protected RxTxSerialConnectionType newConnectionType () {
        return new RxTxSerialConnectionType();
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

}