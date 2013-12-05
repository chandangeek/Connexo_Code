package com.energyict.protocols.mdc.channels.serial.optical.serialio;

import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.protocols.mdc.channels.ConnectionTypePropertiesTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for the {@link SioOpticalConnectionType} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:03
 */
public class SioOpticalConnectionTypeTest extends ConnectionTypePropertiesTest{

    @Override
    protected SioOpticalConnectionType newConnectionType () {
        return new SioOpticalConnectionType();
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
