package com.energyict.mdc.upl.properties;

import java.util.Set;

/**
 * TypedProperties model a set of dynamic properties.
 * Each property has a name and a corresponding value.
 * <p>
 * Property values can be inherited. The inherited values
 * can be overruled on this level. When an overruled value
 * is removed, the value will revert to the inherited value.
 * Note that it is possible to test if a value is inherited or not
 * and if a property has an inherited value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-14 (10:55)
 */
public interface TypedProperties {

    /**
     * Gets the value for the property with the specified name
     * that is either defined at this level or inherited from
     * a parent level.
     *
     * @param propertyName The name of the property
     * @return The value defined at this level or inherited from a parent level
     */
    Object getProperty(String propertyName);

    /**
     * Gets the value for the property with the specified name
     * that is either defined at this level or inherited from
     * a parent level.
     * This is a convenience method for {@link #getProperty(String)}
     * that automatically casts the result to the type
     * of the target expression.
     *
     * @param propertyName The name of the property
     * @return The value defined at this level or inherited from a parent level
     */
    <T> T getTypedProperty(String propertyName);

    /**
     * Gets the value for the property with the specified name
     * that is either defined at this level or inherited from
     * a parent level or returns the specified default value
     * when no level provides a value.
     *
     * @param propertyName The name of the property
     * @return The value defined at this level or inherited from a parent level
     * or the default value when no level provides a value
     */
    Object getProperty(String propertyName, Object defaultValue);

    /**
     * Gets the value for the property with the specified name
     * that is either defined at this level or inherited from
     * a parent level or returns the specified default value
     * when no level provides a value.
     *
     * @param propertyName The name of the property
     * @return The value defined at this level or inherited from a parent level
     * or the default value when no level provides a value
     */
    <T> T getTypedProperty(String propertyName, T defaultValue);

    TypedProperties getInheritedProperties ();

    /**
     * Set a value for the property with the specified name.
     * Note that the value that was inherited will be overruled
     * by the one that is set now. It will remain overruled until
     * the value of the property is removed.
     *
     * @param propertyName The name of the property for which a value is set
     * @param value The value
     * @see #removeProperty(String)
     */
    void setProperty(String propertyName, Object value);

    /**
     * Sets all the properties that are defined by the other
     * TypedProperties, <b>excluding</b> the inherited values.
     *
     * @param otherTypedProperties The other TypedProperties from which value are copied
     */
    void setAllLocalProperties(TypedProperties otherTypedProperties);

    /**
     * Sets all the properties that are defined by the other
     * TypedProperties, <b>including</b> the inherited values.
     *
     * @param otherTypedProperties The other TypedProperties from which value are copied
     */
    void setAllProperties(TypedProperties otherTypedProperties);

    /**
     * Removes the value of the property with the specified name
     * from this level, reverting back to the inherited value if any.
     *
     * @param propertyName The name of the property for which the value is removed
     */
    void removeProperty(String propertyName);

    /**
     * Returns the number of properties that are defined on this level,
     * excluding the number of properties that are inherited from the parent level.
     *
     * @return The number of properties that are defined on this level
     */
    int localSize();

    /**
     * Returns the number of properties that are defined,
     * including the number of properties that are inherited from the parent level.
     *
     * @return The number of properties that are defined
     */
    int size();

    /**
     * Returns the set of property names that are defined at this level,
     * therefore excluding the names of properties that are inherited
     * from a parent level.
     *
     * @return The set of properties that are defined at this level
     */
    Set<String> localPropertyNames();

    /**
     * Returns the set of property names for which a value is available,
     * therefore including the names of properties that are defined
     * at this level or inherited from a parent level.
     *
     * @return The set of properties that are defined at this level
     */
    Set<String> propertyNames();

    /**
     * Tests if there is a value for the property with the specified name
     * defined on this level.
     *
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    boolean hasLocalValueFor(String propertyName);

    /**
     * Tests if the value for the property with the specified name
     * is specified on this level.
     *
     * @param value The value
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    boolean isLocalValueFor(Object value, String propertyName);

    /**
     * Tests if there is a value for the property with the specified name
     * inherited from a parent level.
     *
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    boolean hasInheritedValueFor(String propertyName);

    /**
     * Tests if the value for the property with the specified name
     * is specified on the parent level.
     *
     * @param value The value
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    boolean isInheritedValueFor(Object value, String propertyName);

    /**
     * Tests if there is a value for the property with the specified name
     * defined on this level or inherited from the parent level.
     *
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    boolean hasValueFor(String propertyName);

    /**
     * Tests if there is a value for the property with the specified name
     * defined on this level or inherited from the parent level.
     *
     * @param value The value
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    boolean isValueFor(Object value, String propertyName);

}