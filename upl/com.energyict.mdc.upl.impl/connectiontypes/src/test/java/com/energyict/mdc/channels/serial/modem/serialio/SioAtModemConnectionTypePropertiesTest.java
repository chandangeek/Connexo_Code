/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.channels.AbstractConnectionTypePropertiesTest;
import com.energyict.mdc.upl.properties.PropertySpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SioAtModemConnectionTypePropertiesTest extends AbstractConnectionTypePropertiesTest {

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
            assertPropertySpecsEqual(optionalPropertySpec, uplPropertySpec);
        }
    }
}