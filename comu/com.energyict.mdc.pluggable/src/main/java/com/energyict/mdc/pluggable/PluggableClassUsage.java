/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.TypedProperties;

import java.time.Instant;
import java.util.List;

/**
 * Models the usage of a {@link PluggableClass}
 * and provides the {@link PluggableClassUsageProperty properties}
 * that relate to the usage of that PluggableClass.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-10 (09:56)
 */
public interface PluggableClassUsage
                <T extends HasDynamicProperties,
                PC extends PluggableClass,
                PT extends PluggableClassUsageProperty<T>> extends HasId {

    /**
     * Gets the {@link PluggableClass} that is used here.
     *
     * @return The PluggableClass
     */
    public PC getPluggableClass ();

    /**
     * Gets the {@link PluggableClassUsageProperty} with the provided name.
     *
     * @param propertyName the name of the property
     * @return the requested property or <code>null</code> when property is not found
     */
    public PluggableClassUsageProperty<T> getProperty (String propertyName);

    /**
     * Sets the value of the property with the specified name.
     *
     * @param propertyName the name of the property
     * @param value The property value
     */
    public void setProperty (String propertyName, Object value);

    /**
     * Removes the property with the specified name.
     * In case the property was inherited from the {@link PluggableClass}
     * then this actually means that this ConnectionMethod is reverting
     * the setting of the property back to the PluggableClass level.
     *
     * @param propertyName The name of the property
     */
    public void removeProperty (String propertyName);

    /**
     * Provides the current properties ({@link #getAllProperties(Instant)} in the TypedProperties format.
     *
     * @return the TypedProperties
     */
    public TypedProperties getTypedProperties ();

    /**
     * Gets the list of {@link PluggableClassUsageProperty PluggableClassUsageProperties}
     * that are active on the specified Date.
     *
     * @param date The Date on which the properties should be active
     * @return The List of all properties
     */
    public List<PT> getAllProperties (Instant date);

}