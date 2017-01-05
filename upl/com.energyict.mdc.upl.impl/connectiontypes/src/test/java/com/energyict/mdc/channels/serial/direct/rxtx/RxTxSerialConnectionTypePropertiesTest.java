package com.energyict.mdc.channels.serial.direct.rxtx;

import com.energyict.cpo.PropertySpec;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the properties of the {@link RxTxSerialConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (13:58)
 */
public class RxTxSerialConnectionTypePropertiesTest {

    @Test
    public void testGetOptionalPropertiesIsNotNull () {
        RxTxSerialConnectionType connectionType = new RxTxSerialConnectionType();
        assertThat(connectionType.getOptionalProperties()).isNotNull();
    }

    @Test
    public void testAllOptionalPropertiesAreReturnedByGetPropertySpec () {
        RxTxSerialConnectionType connectionType = new RxTxSerialConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getOptionalProperties()) {
            assertThat(connectionType.getUPLPropertySpec(optionalPropertySpec.getName())).
                    as("Property " + optionalPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getUPLPropertySpec(optionalPropertySpec.getName())).isEqualTo(optionalPropertySpec);
        }
    }

    @Test
    public void testOptionalPropertiesAreNotRequired () {
        RxTxSerialConnectionType connectionType = new RxTxSerialConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getOptionalProperties()) {
            assertThat(connectionType.isRequiredProperty(optionalPropertySpec.getName())).
                    as("Optional property " + optionalPropertySpec.getName() + " is not expected to be required").
                    isFalse();
        }
    }

    @Test
    public void testGetRequiredPropertiesIsNotNull () {
        RxTxSerialConnectionType connectionType = new RxTxSerialConnectionType();
        assertThat(connectionType.getRequiredProperties()).isNotNull();
    }

    @Test
    public void testAllRequiredPropertiesAreReturnedByGetPropertySpec () {
        RxTxSerialConnectionType connectionType = new RxTxSerialConnectionType();
        for (PropertySpec requiredPropertySpec : connectionType.getRequiredProperties()) {
            assertThat(connectionType.getUPLPropertySpec(requiredPropertySpec.getName())).
                    as("Property " + requiredPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getUPLPropertySpec(requiredPropertySpec.getName())).isEqualTo(requiredPropertySpec);
        }
    }

    @Test
    public void testRequiredPropertiesAreRequired () {
        RxTxSerialConnectionType connectionType = new RxTxSerialConnectionType();
        for (PropertySpec requiredPropertySpec : connectionType.getRequiredProperties()) {
            assertThat(connectionType.isRequiredProperty(requiredPropertySpec.getName())).
                    as("Optional property " + requiredPropertySpec.getName() + " is expected to be required").
                    isTrue();
        }
    }

}