/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.direct.serialio;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class SioSerialConnectionTypeTest {

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
        SioSerialConnectionType sioSerialConnectionType = new SioSerialConnectionType(propertySpecService);

        assertFalse(sioSerialConnectionType.allowsSimultaneousConnections());
    }

    @Test
    public void getRequiredKeysTest() {
        SioSerialConnectionType serialConnectionType = new SioSerialConnectionType(propertySpecService);

        // asserts
        assertThat(serialConnectionType.getUPLPropertySpecs()).isNotEmpty();
        assertTrue(serialConnectionType.getUPLPropertySpec(SerialPortConfiguration.PARITY_NAME).get().isRequired());
        assertTrue(serialConnectionType.getUPLPropertySpec(SerialPortConfiguration.BAUDRATE_NAME).get().isRequired());
        assertTrue(serialConnectionType.getUPLPropertySpec(SerialPortConfiguration.NR_OF_STOP_BITS_NAME).get().isRequired());
        assertTrue(serialConnectionType.getUPLPropertySpec(SerialPortConfiguration.NR_OF_DATA_BITS_NAME).get().isRequired());
    }

    @Test
    public void getOptionalPropertiesTest() {
        SioSerialConnectionType serialConnectionType = new SioSerialConnectionType(propertySpecService);

        // asserts
        assertThat(serialConnectionType.getUPLPropertySpecs()).isNotEmpty();
        assertTrue(!serialConnectionType.getUPLPropertySpec(SerialPortConfiguration.FLOW_CONTROL_NAME).get().isRequired());
    }

    @Test
    public void getPossibleValuesInCorrectOrderTest() {
        SioSerialConnectionType serialConnectionType = new SioSerialConnectionType(propertySpecService);

        Optional<PropertySpec> uplPropertySpec = serialConnectionType.getUPLPropertySpec(SerialPortConfiguration.BAUDRATE_NAME);

        assertTrue(uplPropertySpec.isPresent());
        PropertySpec baudrate = uplPropertySpec.get();

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