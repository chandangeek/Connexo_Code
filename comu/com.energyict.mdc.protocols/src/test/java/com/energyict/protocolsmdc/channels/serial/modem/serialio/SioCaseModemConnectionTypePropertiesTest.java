package com.energyict.protocolsmdc.channels.serial.modem.serialio;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolsmdc.channels.serial.modem.AbstractModemTests;
import com.energyict.protocolsmdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the properties of the {@link com.energyict.protocolsmdc.channels.serial.modem.serialio.SioCaseModemConnectionType} component.
 *
 * @author sva
 * @since 30/04/13 - 14:42
 */
public class SioCaseModemConnectionTypePropertiesTest extends AbstractModemTests {

    @Test
    public void testGetOptionalPropertiesIsNotNull() {
        SioAtModemConnectionType connectionType = new SioAtModemConnectionType();
        assertThat(connectionType.getOptionalProperties()).isNotNull();
    }

    @Test
    public void testAllOptionalPropertiesAreReturnedByGetPropertySpec() {
        SioAtModemConnectionType connectionType = new SioAtModemConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getOptionalProperties()) {
            assertThat(connectionType.getPropertySpec(optionalPropertySpec.getName())).
                    as("Property " + optionalPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getPropertySpec(optionalPropertySpec.getName())).isEqualTo(optionalPropertySpec);
        }
    }

    @Test
    public void testOptionalPropertiesAreNotRequired() {
        SioAtModemConnectionType connectionType = new SioAtModemConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getOptionalProperties()) {
            assertThat(connectionType.isRequiredProperty(optionalPropertySpec.getName())).
                    as("Optional property " + optionalPropertySpec.getName() + " is not expected to be required").
                    isFalse();
        }
    }

    @Test
    public void testGetRequiredPropertiesIsNotNull() {
        SioAtModemConnectionType connectionType = new SioAtModemConnectionType();
        assertThat(connectionType.getRequiredProperties()).isNotNull();
    }

    @Test
    public void testAllRequiredPropertiesAreReturnedByGetPropertySpec() {
        SioAtModemConnectionType connectionType = new SioAtModemConnectionType();
        for (PropertySpec requiredPropertySpec : connectionType.getRequiredProperties()) {
            assertThat(connectionType.getPropertySpec(requiredPropertySpec.getName())).
                    as("Property " + requiredPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getPropertySpec(requiredPropertySpec.getName())).isEqualTo(requiredPropertySpec);
        }
    }

    @Test
    public void testRequiredPropertiesAreRequired() {
        SioAtModemConnectionType connectionType = new SioAtModemConnectionType();
        for (PropertySpec requiredPropertySpec : connectionType.getRequiredProperties()) {
            assertThat(connectionType.isRequiredProperty(requiredPropertySpec.getName())).
                    as("Optional property " + requiredPropertySpec.getName() + " is expected to be required").
                    isTrue();
        }
    }

}
