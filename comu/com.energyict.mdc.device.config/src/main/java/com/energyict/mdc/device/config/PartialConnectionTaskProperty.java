package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;

/**
 * Partial version of a ConnectionTaskProperty
 *
 * @author sva
 * @since 21/01/13 - 16:15
 */
@ProviderType
public interface PartialConnectionTaskProperty {

    /**
     * Gets the name of the property for which a value is held.
     *
     * @return The name of the property
     */
    public String getName();

    /**
     * Gets the value of the property.
     *
     * @return The value
     */
    public Object getValue();

    /**
     * Gets the {@link PartialConnectionTask} that owns this {@link PartialConnectionTaskProperty}
     *
     * @return The PartialConnectionTask
     */
    public PartialConnectionTask getPartialConnectionTask();

    void save();
}