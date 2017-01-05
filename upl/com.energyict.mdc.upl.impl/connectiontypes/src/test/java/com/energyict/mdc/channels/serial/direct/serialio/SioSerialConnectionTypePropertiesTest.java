package com.energyict.mdc.channels.serial.direct.serialio;

import com.energyict.cpo.PropertySpec;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the properties of the {@link SioSerialConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (13:58)
 */
public class SioSerialConnectionTypePropertiesTest {

    @Test
    public void testGetOptionalPropertiesIsNotNull () {
        SioSerialConnectionType connectionType = new SioSerialConnectionType();
        assertThat(connectionType.getOptionalProperties()).isNotNull();
    }

    @Test
    public void testAllOptionalPropertiesAreReturnedByGetPropertySpec () {
        SioSerialConnectionType connectionType = new SioSerialConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getOptionalProperties()) {
            assertThat(connectionType.getUPLPropertySpec(optionalPropertySpec.getName())).
                    as("Property " + optionalPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getUPLPropertySpec(optionalPropertySpec.getName())).isEqualTo(optionalPropertySpec);
        }
    }

    @Test
    public void testOptionalPropertiesAreNotRequired () {
        SioSerialConnectionType connectionType = new SioSerialConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getOptionalProperties()) {
            assertThat(connectionType.isRequiredProperty(optionalPropertySpec.getName())).
                    as("Optional property " + optionalPropertySpec.getName() + " is not expected to be required").
                    isFalse();
        }
    }

    @Test
    public void testGetRequiredPropertiesIsNotNull () {
        SioSerialConnectionType connectionType = new SioSerialConnectionType();
        assertThat(connectionType.getRequiredProperties()).isNotNull();
    }

    @Test
    public void testAllRequiredPropertiesAreReturnedByGetPropertySpec () {
        SioSerialConnectionType connectionType = new SioSerialConnectionType();
        for (PropertySpec requiredPropertySpec : connectionType.getRequiredProperties()) {
            assertThat(connectionType.getUPLPropertySpec(requiredPropertySpec.getName())).
                    as("Property " + requiredPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getUPLPropertySpec(requiredPropertySpec.getName())).isEqualTo(requiredPropertySpec);
        }
    }

    @Test
    public void testRequiredPropertiesAreRequired () {
        SioSerialConnectionType connectionType = new SioSerialConnectionType();
        for (PropertySpec requiredPropertySpec : connectionType.getRequiredProperties()) {
            assertThat(connectionType.isRequiredProperty(requiredPropertySpec.getName())).
                    as("Optional property " + requiredPropertySpec.getName() + " is expected to be required").
                    isTrue();
        }
    }

}