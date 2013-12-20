package com.energyict.protocols.mdc.channels.ip.socket;

import com.energyict.mdc.dynamic.PropertySpec;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the properties of the {@link OutboundTcpIpConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (08:52)
 */
public class TcpIpConnectionTypePropertiesTest {

    @Test
    public void testGetPropertiesisNotNull () {
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType();
        assertThat(connectionType.getPropertySpecs()).isNotNull();
    }

    @Test
    public void testAllPropertiesAreReturnedByGetPropertySpec () {
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType();
        for (PropertySpec propertySpec : connectionType.getPropertySpecs()) {
            assertThat(connectionType.getPropertySpec(propertySpec.getName())).
                    as("Property " + propertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getPropertySpec(propertySpec.getName())).isEqualTo(propertySpec);
        }
    }

}