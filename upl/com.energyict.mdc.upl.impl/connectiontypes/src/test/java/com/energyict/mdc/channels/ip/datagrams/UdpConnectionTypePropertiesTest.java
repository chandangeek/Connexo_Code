/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.ip.datagrams;

import com.energyict.mdc.channels.AbstractConnectionTypePropertiesTest;
import com.energyict.mdc.upl.properties.PropertySpec;
import org.junit.Test;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the properties of the {@link OutboundUdpConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (13:56)
 */
public class UdpConnectionTypePropertiesTest extends AbstractConnectionTypePropertiesTest {

    @Test
    public void testGetPropertiesIsNotNull() {
        OutboundUdpConnectionType connectionType = new OutboundUdpConnectionType(propertySpecService);
        assertThat(connectionType.getUPLPropertySpecs()).isNotNull();
    }

    @Test
    public void testAllPropertiesAreReturnedByGetPropertySpec() {
        OutboundUdpConnectionType connectionType = new OutboundUdpConnectionType(propertySpecService);
        for (PropertySpec optionalPropertySpec : connectionType.getUPLPropertySpecs()) {
            Optional<PropertySpec> uplPropertySpec = connectionType.getUPLPropertySpec(optionalPropertySpec.getName());
            assertPropertySpecsEqual(optionalPropertySpec, uplPropertySpec);
        }
    }
}