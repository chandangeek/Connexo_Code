/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests the properties of the {@link OutboundTcpIpConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (08:52)
 */
public class TcpIpConnectionTypePropertiesTest {

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
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType(propertySpecService);
        assertThat(connectionType.getUPLPropertySpecs()).isNotNull();
    }

    @Test
    public void testAllPropertiesAreReturnedByGetPropertySpec() {
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType(propertySpecService);
        for (PropertySpec optionalPropertySpec : connectionType.getUPLPropertySpecs()) {
            Optional<PropertySpec> uplPropertySpec = connectionType.getUPLPropertySpec(optionalPropertySpec.getName());
            assertTrue(uplPropertySpec.isPresent());
            assertThat(uplPropertySpec.get()).isEqualTo(optionalPropertySpec);
        }
    }
}