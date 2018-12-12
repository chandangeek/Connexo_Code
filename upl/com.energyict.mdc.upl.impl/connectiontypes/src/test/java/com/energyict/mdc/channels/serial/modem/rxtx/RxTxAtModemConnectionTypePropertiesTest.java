/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.modem.rxtx;

import com.energyict.mdc.channels.AbstractConnectionTypePropertiesTest;
import com.energyict.mdc.upl.properties.PropertySpec;
import org.junit.Test;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the properties of the {@link RxTxAtModemConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (14:00)
 */
public class RxTxAtModemConnectionTypePropertiesTest extends AbstractConnectionTypePropertiesTest {

    @Test
    public void testGetPropertiesIsNotNull() {
        RxTxAtModemConnectionType connectionType = new RxTxAtModemConnectionType(propertySpecService);
        assertThat(connectionType.getUPLPropertySpecs()).isNotNull();
    }

    @Test
    public void testAllPropertiesAreReturnedByGetPropertySpec() {
        RxTxAtModemConnectionType connectionType = new RxTxAtModemConnectionType(propertySpecService);
        for (PropertySpec optionalPropertySpec : connectionType.getUPLPropertySpecs()) {
            Optional<PropertySpec> uplPropertySpec = connectionType.getUPLPropertySpec(optionalPropertySpec.getName());
            assertPropertySpecsEqual(optionalPropertySpec, uplPropertySpec);
        }
    }
}