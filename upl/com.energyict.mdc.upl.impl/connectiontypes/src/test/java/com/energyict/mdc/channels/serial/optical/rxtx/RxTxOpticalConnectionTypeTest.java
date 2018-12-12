/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.optical.rxtx;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.AbstractConnectionTypePropertiesTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RxTxOpticalConnectionTypeTest extends AbstractConnectionTypePropertiesTest {

    @Test
    public void allowSimultaneousConnectionsTest() {
        RxTxOpticalConnectionType sioOpticalConnectionType = new RxTxOpticalConnectionType(propertySpecService);

        assertFalse(sioOpticalConnectionType.allowsSimultaneousConnections());
    }

    @Test
    public void getRequiredKeysTest() {
        RxTxOpticalConnectionType sioOpticalConnectionType = new RxTxOpticalConnectionType(propertySpecService);

        // asserts
        assertThat(sioOpticalConnectionType.getUPLPropertySpecs()).isNotEmpty();
        assertTrue(sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.PARITY_NAME).get().isRequired());
        assertTrue(sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.BAUDRATE_NAME).get().isRequired());
        assertTrue(sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.NR_OF_STOP_BITS_NAME).get().isRequired());
        assertTrue(sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.NR_OF_DATA_BITS_NAME).get().isRequired());
    }

    @Test
    public void getOptionalPropertiesTest() {
        RxTxOpticalConnectionType sioOpticalConnectionType = new RxTxOpticalConnectionType(propertySpecService);

        // asserts
        assertThat(sioOpticalConnectionType.getUPLPropertySpecs()).isNotEmpty();
        assertTrue(!sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.FLOW_CONTROL_NAME).get().isRequired());
    }
}