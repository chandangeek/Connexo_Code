package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionTaskPropertyImpl;

import com.energyict.cbo.ApplicationException;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author sva
 * @since 29/04/13 - 16:13
 */
public class TypedPEMPModemPropertiesTest {

    @Test
    public void tesValidateAndSetValidProperties() throws Exception {
        TypedPEMPModemProperties modemProperties = new TypedPEMPModemProperties();
        List<ConnectionTaskProperty> properties = new ArrayList<>();
        properties.add(new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.MODEM_CONFIGURATION_KEY, PEMPModemConfiguration.NHC.getKey(), null, null));

        // Business method
        modemProperties.validateAndSetProperties(properties);

        // Asserts
        assertEquals("Only expecting 1 property (the modemConfiguration property)", 1, modemProperties.getAllProperties().size());
        assertEquals("Should return the NHC modem configuration", PEMPModemConfiguration.NHC, modemProperties.getPEMPModemConfiguration());
    }

    @Test(expected = ApplicationException.class)
    public void tesValidateAndSetInvalidProperties() throws Exception {
        TypedPEMPModemProperties modemProperties = new TypedPEMPModemProperties();
        List<ConnectionTaskProperty> properties = new ArrayList<>();
        properties.add(new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.MODEM_CONFIGURATION_KEY, "INVALID KEY", null, null));

        // Business method - should throw an ApplicationException, cause the modemConfigurationKey property value is not a known key.
        modemProperties.validateAndSetProperties(properties);
    }
}
