/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class SioAtModemConnectionTypePropertiesTest extends AbstractModemTests {

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
    public void testGetPropertiesIsNotNull() {
        SioAtModemConnectionType connectionType = new SioAtModemConnectionType(propertySpecService);
        assertThat(connectionType.getUPLPropertySpecs()).isNotNull();
    }

    @Test
    public void testAllPropertiesAreReturnedByGetPropertySpec() {
        SioAtModemConnectionType connectionType = new SioAtModemConnectionType(propertySpecService);
        for (PropertySpec optionalPropertySpec : connectionType.getUPLPropertySpecs()) {
            Optional<PropertySpec> uplPropertySpec = connectionType.getUPLPropertySpec(optionalPropertySpec.getName());
            assertTrue(uplPropertySpec.isPresent());
            assertThat(uplPropertySpec.get()).isEqualTo(optionalPropertySpec);
        }
    }
}