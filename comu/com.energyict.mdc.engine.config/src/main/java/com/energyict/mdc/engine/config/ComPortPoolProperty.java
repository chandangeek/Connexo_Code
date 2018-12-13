package com.energyict.mdc.engine.config;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ComPortPoolProperty {
    /**
     * Gets the name of the property for which a value is held.
     *
     * @return The name of the property
     */
    String getName();

    /**
     * Gets the value of the property.
     *
     * @return The value
     */
    Object getValue();

    /**
     * Gets the {@link ComPortPool} that owns this {@link ComPortPoolProperty}
     *
     * @return The ComPortPool
     */
    ComPortPool getComPortPool();

    void save();
}
