/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.direct.rxtx;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.upl.properties.PropertySpecService;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RxTxSerialConnectionTypeTest {

    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocksAndFactories() {
        propertySpecService = mock(PropertySpecService.class);
/*
        //TODO
        PropertySpecBuilderWizard.NlsOptions propertySpecBuilder = new PropertySpecBuilderImpl();
        when(propertySpecService.encryptedStringSpec()).thenReturn(propertySpecBuilder);
*/
    }

    @Test
    public void allowSimultaneousConnectionsTest() {
        RxTxSerialConnectionType sioOpticalConnectionType = new RxTxSerialConnectionType(propertySpecService);

        assertFalse(sioOpticalConnectionType.allowsSimultaneousConnections());
    }

    @Test
    public void getRequiredKeysTest() {
        RxTxSerialConnectionType sioOpticalConnectionType = new RxTxSerialConnectionType(propertySpecService);

        // asserts
        assertThat(sioOpticalConnectionType.getUPLPropertySpecs()).isNotEmpty();
        assertTrue(sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.PARITY_NAME).get().isRequired());
        assertTrue(sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.BAUDRATE_NAME).get().isRequired());
        assertTrue(sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.NR_OF_STOP_BITS_NAME).get().isRequired());
        assertTrue(sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.NR_OF_DATA_BITS_NAME).get().isRequired());
    }

    @Test
    public void getOptionalPropertiesTest() {
        RxTxSerialConnectionType sioOpticalConnectionType = new RxTxSerialConnectionType(propertySpecService);

        // asserts
        assertThat(sioOpticalConnectionType.getUPLPropertySpecs()).isNotEmpty();
        assertTrue(!sioOpticalConnectionType.getUPLPropertySpec(SerialPortConfiguration.FLOW_CONTROL_NAME).get().isRequired());
    }
}