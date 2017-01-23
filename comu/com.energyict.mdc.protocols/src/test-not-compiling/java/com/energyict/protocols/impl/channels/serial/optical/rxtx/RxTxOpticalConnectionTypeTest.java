package com.energyict.protocols.impl.channels.serial.optical.rxtx;

import com.energyict.protocols.impl.channels.ConnectionTypePropertiesTest;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for the {@link RxTxOpticalConnectionType} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:05
 */
public class RxTxOpticalConnectionTypeTest extends ConnectionTypePropertiesTest {

    @Override
    protected RxTxOpticalConnectionType newConnectionType () {
        return new RxTxOpticalConnectionType();
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
