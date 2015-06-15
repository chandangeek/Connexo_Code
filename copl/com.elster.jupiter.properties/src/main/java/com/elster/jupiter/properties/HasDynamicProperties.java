package com.elster.jupiter.properties;

import java.util.List;

/**
 * Models the behavior of a component that has dynamic configuration.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-27 (14:44)
 */
public interface HasDynamicProperties {

    /**
     * Gets the dynamic {@link PropertySpec}s.
     *
     * @return The List of PropertySpec
     */
    List<PropertySpec> getPropertySpecs ();

    /**
     * Returns the {@link PropertySpec} with the specified name
     * or <code>null</code> if no such PropertySpec exists.
     *
     * @param name The name of the property specification
     * @return The PropertySpec or <code>null</code>
     *         if no such PropertySpec exists
     */
    default PropertySpec getPropertySpec (String name) {
        return getPropertySpecs().stream()
                .filter(propertySpec -> propertySpec.getName().equals(name))
                .findAny()
                .orElse(null);
    }

}
