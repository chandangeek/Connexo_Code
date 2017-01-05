package com.energyict.mdc.upl.properties;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;
import java.util.Optional;

/**
 * Models the behavior of a component that has dynamic configuration.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-28 (15:18)
 */
@ConsumerType
public interface HasDynamicProperties {

    /**
     * Gets the dynamic {@link PropertySpec}s.
     *
     * @return The List of PropertySpec
     */
    List<PropertySpec> getUPLPropertySpecs();

    /**
     * Returns the {@link PropertySpec} with the specified name
     * or an empty Optional if no such PropertySpec exists.
     *
     * @param name The name of the property specification
     * @return The PropertySpec or <code>Optional.empty()</code>
     * if no such PropertySpec exists
     */
    default Optional<PropertySpec> getUPLPropertySpec(String name) {
        return getUPLPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getName().equals(name))
                .findAny();
    }

    /**
     * <p>
     * Sets the protocol specific properties, validating the values against
     * the appropriate {@link com.energyict.mdc.upl.properties.PropertySpec}.
     * Note that a property value that does not relate to a PropertySpec is ignored.
     * </p>
     * <p>
     * This method can also be called at device configuration time
     * to check the validity of the configured values.</p>
     *
     * @param properties contains a set of protocol specific key value pairs
     * @see com.energyict.mdc.upl.properties.PropertySpec#validateValue(Object)
     */
    void setUPLProperties(TypedProperties properties) throws PropertyValidationException;

}