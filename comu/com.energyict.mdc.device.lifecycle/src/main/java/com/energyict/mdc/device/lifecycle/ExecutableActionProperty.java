package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.properties.PropertySpec;

/**
 * Models a property of an {@link ExecutableAction}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-23 (16:25)
 */
public interface ExecutableActionProperty {

    /**
     * Gets the {@link PropertySpec}.
     *
     * @return The PropertySpec
     */
    public PropertySpec getPropertySpec();

    /**
     * Gets the value of this property.
     *
     * @return The value of this property
     */
    public Object getValue();

}