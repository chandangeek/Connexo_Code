package com.energyict.mdc.pluggable;

import com.elster.jupiter.properties.PropertySpec;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TypedProperties;

import java.util.Date;
import java.util.List;

/**
 * Registers a java class that implements some kind of {@link Pluggable} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-27 (14:56)
 */
public interface PluggableClass extends HasId {

    /**
     * Returns the name of this PluggableClass.
     *
     * @return The name
     */
    public String getName();

    public void setName(String name);

    /**
     * Returns the type of Pluggable that is implemented by the java class.
     *
     * @return The PluggableClassType
     */
    public PluggableClassType getPluggableClassType ();

    /**
     * Returns the name of the java implementation class.
     *
     * @return the java class name
     */
    public String getJavaClassName ();

    /**
     * Gets the Date on which this PluggableClass was created or last modified.
     *
     * @return The Date on which this PluggableClass was created or last modified
     */
    public Date getModificationDate();

    /**
     * Returns the dynamic properties of this PluggableClass.
     *
     * @return The TypedProperties
     */
    public TypedProperties getProperties(List<PropertySpec> propertySpecs);

    /**
     * Sets the value of a single property of this PluggableClass.
     *
     * @param propertySpec The PropertySpec
     * @param value The value of the property
     */
    public void setProperty(PropertySpec propertySpec, Object value);

    /**
     * Removes the value of a single property of this PluggableClass.
     *
     * @param propertySpec The PropertySpec
     */
    public void removeProperty(PropertySpec propertySpec);

    public void save();

    /**
     * Deletes this PluggableClass.
     *
     */
    public void delete();

}