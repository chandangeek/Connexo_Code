package com.energyict.protocols.mdc.channels.ip.socket;

import com.elster.jupiter.properties.PropertySpec;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the properties of the {@link TcpIpPostDialConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (08:48)
 */
public class TcpIpPostDialConnectionTypePropertiesTest {

    @Test
    public void testGetPropertiesisNotNull () {
        TcpIpPostDialConnectionType connectionType = new TcpIpPostDialConnectionType();
        assertThat(connectionType.getPropertySpecs()).isNotNull();
    }

    @Test
    public void testAllPropertiesAreReturnedByGetPropertySpec () {
        TcpIpPostDialConnectionType connectionType = new TcpIpPostDialConnectionType();
        for (PropertySpec propertySpec : connectionType.getPropertySpecs()) {
            assertThat(connectionType.getPropertySpec(propertySpec.getName())).
                    as("Property " + propertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getPropertySpec(propertySpec.getName())).isEqualTo(propertySpec);
        }
    }

}