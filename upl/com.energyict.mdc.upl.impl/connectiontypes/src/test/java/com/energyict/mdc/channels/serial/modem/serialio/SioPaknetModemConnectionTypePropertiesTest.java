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

/**
 * Tests the properties of the {@link SioPaknetModemConnectionType} component.
 *
 * @author sva
 * @since 15/04/13 - 11:09
 */
@RunWith(MockitoJUnitRunner.class)
public class SioPaknetModemConnectionTypePropertiesTest extends AbstractConnectionTypePropertiesTest {

    @Test
    public void testGetPropertiesIsNotNull() {
        SioPaknetModemConnectionType connectionType = new SioPaknetModemConnectionType(propertySpecService);
        assertThat(connectionType.getUPLPropertySpecs()).isNotNull();
    }

    @Test
    public void testAllPropertiesAreReturnedByGetPropertySpec() {
        SioPaknetModemConnectionType connectionType = new SioPaknetModemConnectionType(propertySpecService);
        for (PropertySpec optionalPropertySpec : connectionType.getUPLPropertySpecs()) {
            Optional<PropertySpec> uplPropertySpec = connectionType.getUPLPropertySpec(optionalPropertySpec.getName());
            assertPropertySpecsEqual(optionalPropertySpec, uplPropertySpec);
        }
    }
}