package com.energyict.protocolsmdc.channels.serial.modem.serialio;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolsmdc.channels.serial.modem.AbstractModemTests;
import com.energyict.protocolsmdc.channels.serial.modem.serialio.SioPaknetModemConnectionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the properties of the {@link com.energyict.protocolsmdc.channels.serial.modem.serialio.SioPaknetModemConnectionType} component.
 *
 * @author sva
 * @since 15/04/13 - 11:09
 */
@RunWith(MockitoJUnitRunner.class)
public class SioPaknetModemConnectionTypePropertiesTest extends AbstractModemTests{

    @Test
    public void testGetOptionalPropertiesIsNotNull () {
        SioPaknetModemConnectionType connectionType = new SioPaknetModemConnectionType();
        assertThat(connectionType.getOptionalProperties()).isNotNull();
    }

    @Test
    public void testAllOptionalPropertiesAreReturnedByGetPropertySpec () {
        SioPaknetModemConnectionType connectionType = new SioPaknetModemConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getOptionalProperties()) {
            assertThat(connectionType.getPropertySpec(optionalPropertySpec.getName())).
                    as("Property " + optionalPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getPropertySpec(optionalPropertySpec.getName())).isEqualTo(optionalPropertySpec);
        }
    }

    @Test
    public void testOptionalPropertiesAreNotRequired () {
        SioPaknetModemConnectionType connectionType = new SioPaknetModemConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getOptionalProperties()) {
            assertThat(connectionType.isRequiredProperty(optionalPropertySpec.getName())).
                    as("Optional property " + optionalPropertySpec.getName() + " is not expected to be required").
                    isFalse();
        }
    }

    @Test
    public void testGetRequiredPropertiesIsNotNull () {
        SioPaknetModemConnectionType connectionType = new SioPaknetModemConnectionType();
        assertThat(connectionType.getRequiredProperties()).isNotNull();
    }

    @Test
    public void testAllRequiredPropertiesAreReturnedByGetPropertySpec () {
        SioPaknetModemConnectionType connectionType = new SioPaknetModemConnectionType();
        for (PropertySpec requiredPropertySpec : connectionType.getRequiredProperties()) {
            assertThat(connectionType.getPropertySpec(requiredPropertySpec.getName())).
                    as("Property " + requiredPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getPropertySpec(requiredPropertySpec.getName())).isEqualTo(requiredPropertySpec);
        }
    }

    @Test
    public void testRequiredPropertiesAreRequired () {
        SioPaknetModemConnectionType connectionType = new SioPaknetModemConnectionType();
        for (PropertySpec requiredPropertySpec : connectionType.getRequiredProperties()) {
            assertThat(connectionType.isRequiredProperty(requiredPropertySpec.getName())).
                    as("Optional property " + requiredPropertySpec.getName() + " is expected to be required").
                    isTrue();
        }
    }

}