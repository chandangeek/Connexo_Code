package com.energyict.mdc.pluggable;

import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.HasDynamicProperties;

import java.util.Date;
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
                PT extends PluggableClassUsageProperty<T>>
        extends IdBusinessObject {

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
     * Provides the current properties ({@link #getAllProperties(Date)} in the TypedProperties format.
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
    public List<PT> getAllProperties (Date date);

}