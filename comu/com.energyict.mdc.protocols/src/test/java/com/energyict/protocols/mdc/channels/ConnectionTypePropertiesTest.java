package com.energyict.protocols.mdc.channels;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Serves as the root for all test components
 * that test the properties of a {@link ConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-05 (08:37)
 */
public abstract class ConnectionTypePropertiesTest {

    @Test
    public void testNumberOfPropertySpecs () {
        ConnectionType connectionType = newConnectionType();

        // Business method
        List<PropertySpec> propertySpecs = connectionType.getPropertySpecs();

        // Asserts
        assertThat(propertySpecs).hasSize(requiredPropertyNames().size() + optionalPropertyNames().size());
    }

    @Test
    public void getNumberOfRequiredPropertiesTest () {
        ConnectionType connectionType = newConnectionType();

        // Business method
        List<PropertySpec> allPropertySpecs = connectionType.getPropertySpecs();

        List<PropertySpec> required = this.getRequiredProperties(allPropertySpecs);

        // Asserts
        assertThat(required).hasSize(this.requiredPropertyNames().size());
    }

    @Test
    public void getRequiredPropertyNamesTest () {
        ConnectionType connectionType = newConnectionType();

        // Business method
        List<PropertySpec> allPropertySpecs = connectionType.getPropertySpecs();

        List<PropertySpec> required = this.getRequiredProperties(allPropertySpecs);

        // Asserts
        for (String propertyName : this.requiredPropertyNames()) {
            assertThat(this.getPropertyNames(required)).contains(propertyName);
        }
    }

    @Test
    public void getGetRequiredPropertyTest () {
        ConnectionType connectionType = newConnectionType();

        // Business method
        for (String propertyName : this.requiredPropertyNames()) {
            PropertySpec requiredPropertySpec = connectionType.getPropertySpec(propertyName);

            // Asserts
            assertThat(requiredPropertySpec).isNotNull();
            assertThat(requiredPropertySpec.isRequired()).isTrue();
        }
    }

    @Test
    public void getNumberOfOptionalPropertiesTest () {
        ConnectionType connectionType = newConnectionType();

        // Business method
        List<PropertySpec> allPropertySpecs = connectionType.getPropertySpecs();

        List<PropertySpec> optional = this.getOptionalProperties(allPropertySpecs);

        // Asserts
        assertThat(optional).hasSize(this.optionalPropertyNames().size());
    }

    @Test
    public void getOptionalPropertyNamesTest () {
        ConnectionType connectionType = newConnectionType();

        // Business method
        List<PropertySpec> allPropertySpecs = connectionType.getPropertySpecs();

        List<PropertySpec> optional = this.getOptionalProperties(allPropertySpecs);

        // Asserts
        for (String propertyName : this.optionalPropertyNames()) {
            assertThat(this.getPropertyNames(optional)).contains(propertyName);
        }
    }

    @Test
    public void getGetOptionalPropertyTest () {
        ConnectionType connectionType = newConnectionType();

        // Business method
        for (String propertyName : this.optionalPropertyNames()) {
            PropertySpec optionalPropertySpec = connectionType.getPropertySpec(propertyName);

            // Asserts
            assertThat(optionalPropertySpec).isNotNull();
            assertThat(optionalPropertySpec.isRequired()).isFalse();
        }
    }

    @Test
    public void getNonExistingPropertyTest () {
        ConnectionType connectionType = newConnectionType();

        // Business method
        PropertySpec propertySpec = connectionType.getPropertySpec(this.getClass().getName());

        // Asserts
        assertThat(propertySpec).isNull();
    }

    protected abstract ConnectionType newConnectionType ();

    protected abstract Set<String> requiredPropertyNames ();

    protected abstract Set<String> optionalPropertyNames ();

    private List<PropertySpec> getRequiredProperties (List<PropertySpec> propertySpecs) {
        List<PropertySpec> required = new ArrayList<>(propertySpecs.size());
        for (PropertySpec propertySpec : propertySpecs) {
            if (propertySpec.isRequired()) {
                required.add(propertySpec);
            }
        }
        return required;
    }

    private List<PropertySpec> getOptionalProperties (List<PropertySpec> propertySpecs) {
        List<PropertySpec> optional = new ArrayList<>(propertySpecs.size());
        for (PropertySpec propertySpec : propertySpecs) {
            if (!propertySpec.isRequired()) {
                optional.add(propertySpec);
            }
        }
        return optional;
    }

    private List<String> getPropertyNames (List<PropertySpec> propertySpecs) {
        List<String> names = new ArrayList<>(propertySpecs.size());
        for (PropertySpec propertySpec : propertySpecs) {
            names.add(propertySpec.getName());
        }
        return names;
    }

}