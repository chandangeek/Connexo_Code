package com.energyict.protocols.mdc.channels.ip.datagrams;

import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the properties of the {@link OutboundUdpConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (13:56)
 */
public class UdpConnectionTypePropertiesTest {

    @Test
    public void testGetPropertiesisNotNull () {
        OutboundUdpConnectionType connectionType = new OutboundUdpConnectionType();
        assertThat(connectionType.getPropertySpecs()).isNotNull();
    }

    @Test
    public void testAllPropertiesAreReturnedByGetPropertySpec () {
        OutboundUdpConnectionType connectionType = new OutboundUdpConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getPropertySpecs()) {
            assertThat(connectionType.getPropertySpec(optionalPropertySpec.getName())).
                    as("Property " + optionalPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getPropertySpec(optionalPropertySpec.getName())).isEqualTo(optionalPropertySpec);
        }
    }

}