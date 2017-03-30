/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.TypedProperties;

import java.time.Instant;
import java.util.List;

/**
 * Registers a java class that implements some kind of {@link Pluggable} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-27 (14:56)
 */
public interface PluggableClass extends HasId, HasName {

    void setName(String name);

    /**
     * Returns the type of Pluggable that is implemented by the java class.
     *
     * @return The PluggableClassType
     */
    PluggableClassType getPluggableClassType();

    /**
     * Returns the name of the java implementation class.
     *
     * @return the java class name
     */
    String getJavaClassName();

    /**
     * Gets the Date on which this PluggableClass was created or last modified.
     *
     * @return The Date on which this PluggableClass was created or last modified
     */
    Instant getModificationDate();

    /**
     * Returns the dynamic properties of this PluggableClass.
     *
     * @return The TypedProperties
     */
    TypedProperties getProperties(List<PropertySpec> propertySpecs);

    /**
     * Sets the value of a single property of this PluggableClass.
     *
     * @param propertySpec The PropertySpec
     * @param value The value of the property
     */
    void setProperty(PropertySpec propertySpec, Object value);

    /**
     * Removes the value of a single property of this PluggableClass.
     *
     * @param propertySpec The PropertySpec
     */
    void removeProperty(PropertySpec propertySpec);

    void save();

    /**
     * Deletes this PluggableClass.
     *
     */
    void delete();

    long getEntityVersion();
}