package com.energyict.protocols.impl.channels.ip.socket;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.mdc.services.impl.Bus;
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
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType(Bus.getPropertySpecService(), Bus.getSocketService());
        assertThat(connectionType.getPropertySpecs()).isNotNull();
    }

    @Test
    public void testAllPropertiesAreReturnedByGetPropertySpec () {
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType(Bus.getPropertySpecService(), Bus.getSocketService());
        for (PropertySpec propertySpec : connectionType.getPropertySpecs()) {
            assertThat(connectionType.getPropertySpec(propertySpec.getName())).
                    as("Property " + propertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getPropertySpec(propertySpec.getName())).isEqualTo(propertySpec);
        }
    }

}