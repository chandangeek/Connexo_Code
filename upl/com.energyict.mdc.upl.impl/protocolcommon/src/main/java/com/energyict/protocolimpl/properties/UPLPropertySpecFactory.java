package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.PropertySpec;

/**
 * Provides factory services for {@link com.energyict.mdc.upl.properties.PropertySpec}s
 * as defined by the universal protocol layer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-28 (16:34)
 */
public final class UPLPropertySpecFactory {

    public static PropertySpec integral(String name, boolean required) {
        return new IntegerPropertySpec(name, required);
    }

    public static PropertySpec string(String name, boolean required) {
        return new StringPropertySpec(name, required);
    }

    public static PropertySpec string(String name, boolean required, int maxLength) {
        return new StringPropertySpec(name, required, maxLength);
    }

    // Hide utility class constructor
    private UPLPropertySpecFactory() {}

}