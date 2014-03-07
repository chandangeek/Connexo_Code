package com.energyict.mdc.common;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * TypedProperties model a set of dynamic properties.
 * Each property has a name (aka the key) and fa corresponding value.
 * <p>
 * Property values can be inherited. The inherited values
 * can be overruled on this level. When an overruled value
 * is removed, the value will revert to the inherited value.
 * Note that is possible to test if a value is inherited or not
 * and if a property has an inherited value.
 *
 * @author Joost Bruneel (jbr), Rudi Vankeirsbilck (rudi)
 * @since 2012-05-10 (14:11)
 */
public class TypedProperties {

    private Map<String, Object> props = new HashMap<>();
    private TypedProperties inheritedProperties;
    private UnmodifiableTypedProperties unmodifiableView;

    /**
     * Returns a new empty TypedProperties.
     *
     * @return The empty TypedProperties
     */
    public static TypedProperties empty () {
        return new TypedProperties();
    }

    /**
     * Creates a new empty TypedProperties that will inherit
     * from another set of TypedProperties. The inherited values
     * can be overruled on this level. When an overruled value
     * is removed, the value will revert to the inherited value.
     *
     * @param inheritedProperties The set of properties that will be inherited
     * @return The TypedProperties with inherited properties of which none have been overrule yet
     */
    public static TypedProperties inheritingFrom (TypedProperties inheritedProperties) {
        TypedProperties typedProperties = empty();
        typedProperties.inheritedProperties = inheritedProperties;
        return typedProperties;
    }

    /**
     * Creates a new TypeProperties object that is an exact copy
     * of the specified {@link TypedProperties}.
     *
     * @param other The other TypedProperties
     * @return The copy of the TypedProperties
     */
    public static TypedProperties copyOf (TypedProperties other) {
        TypedProperties typedProperties = empty();
        typedProperties.setAllProperties(other);
        return typedProperties;
    }

    /**
     * Creates a new TypeProperties object that is an exact copy
     * of the specified simple {@link Properties}.
     * Note that it is assumed that the keys and values
     * of the simple Properties are both Strings
     * and this will throw a ClassCastException
     * when that is not the case.
     *
     * @param simpleProperties The other TypedProperties
     * @return The copy of the TypedProperties
     */
    public static TypedProperties copyOf (Properties simpleProperties) {
        TypedProperties typedProperties = empty();
        for (Object key : simpleProperties.keySet()) {
            typedProperties.setProperty((String) key, simpleProperties.getProperty((String) key));
        }
        return typedProperties;
    }

    /**
     * @deprecated Used for serialization purposes only,
     *             use the factory method empty() instead.
     * @see #empty()
     */
    @Deprecated
    public TypedProperties() {
        super();
    }

    /**
     * Set a value for the property with the specified name.
     * Note that the value that was inherited will be overrule
     * by the one that is set now. It will remain overruled until
     * the value of the property is removed.
     *
     * @param propertyName The name of the property for which a value is set
     * @param value The value
     * @see #removeProperty(String)
     */
    public void setProperty(String propertyName, Object value) {
        this.props.put(propertyName, value);
    }

    /**
     * Sets all the properties that are defined by the other
     * TypedProperties, excluding the inherited values.
     *
     * @param otherTypedProperties The other TypedProperties from which value are copied
     */
    public void setAllProperties (TypedProperties otherTypedProperties) {
        this.props.putAll(otherTypedProperties.props);
    }

    /**
     * Gets the value for the property with the specified name
     * that is either defined at this level or inherited from
     * a parent level.
     *
     * @param propertyName The name of the property
     * @return The value defined at this level or inherited from a parent level
     */
    public Object getProperty(String propertyName) {
        Object valueFromThisLevel = this.props.get(propertyName);
        if (valueFromThisLevel == null) {
            if (this.inheritedProperties != null) {
                return this.inheritedProperties.getProperty(propertyName);
            }
            else {
                return null;
            }
        }
        else {
            return valueFromThisLevel;
        }
    }

    /**
     * Gets the value for the property with the specified name
     * that is either defined at this level or inherited from
     * a parent level.
     *
     * @param propertyName The name of the property
     * @return The value defined at this level or inherited from a parent level
     */
    public <T> T getTypedProperty (String propertyName) {
        Object valueFromThisLevel = this.props.get(propertyName);
        if (valueFromThisLevel == null) {
            if (this.inheritedProperties != null) {
                return this.inheritedProperties.getTypedProperty(propertyName);
            }
            else {
                return null;
            }
        }
        else {
            return (T) valueFromThisLevel;
        }
    }

    /**
     * Gets the value for the property with the specified name
     * that is either defined at this level or inherited from
     * a parent level or returns the specified default value
     * when no level provides a value.
     *
     * @param propertyName The name of the property
     * @return The value defined at this level or inherited from a parent level
     *         or the default value when no level provides a value
     */
    public Object getProperty(String propertyName, Object defaultValue) {
        Object valueFromThisLevel = this.props.get(propertyName);
        if (valueFromThisLevel == null) {
            if (this.inheritedProperties != null) {
                return this.inheritedProperties.getProperty(propertyName, defaultValue);
            }
            else {
                return defaultValue;
            }
        }
        else {
            return valueFromThisLevel;
        }
    }

    /**
     * Gets the value for the property with the specified name
     * that is either defined at this level or inherited from
     * a parent level or returns the specified default value
     * when no level provides a value.
     *
     * @param propertyName The name of the property
     * @return The value defined at this level or inherited from a parent level
     *         or the default value when no level provides a value
     */
    public <T> T getTypedProperty (String propertyName, T defaultValue) {
        Object valueFromThisLevel = this.props.get(propertyName);
        if (valueFromThisLevel == null) {
            if (this.inheritedProperties != null) {
                return this.inheritedProperties.getTypedProperty(propertyName, defaultValue);
            }
            else {
                return defaultValue;
            }
        }
        else {
            return (T) valueFromThisLevel;
        }
    }

    /**
     * Removes the value of the property with the specified name
     * from this level, reverting back to the inherited value if any.
     *
     * @param propertyName The name of the property for which the value is removed
     */
    public void removeProperty(String propertyName) {
        this.props.remove(propertyName);
    }

    public TypedProperties clone() {
        return TypedProperties.copyOf(this);
    }

    /**
     * Returns the number of properties that are defined on this level,
     * excluding the number of properties that are inherited from the parent level.
     *
     * @return The number of properties that are defined on this level
     */
    public int localSize () {
        return props.size();
    }

    /**
     * Returns the number of properties that are defined,
     * including the number of properties that are inherited from the parent level.
     *
     * @return The number of properties that are defined
     */
    public int size () {
        int localSize = this.localSize();
        if (this.inheritedProperties != null) {
            return localSize + this.inheritedProperties.size();
        }
        else {
            return localSize;
        }
    }

    public String getStringProperty(String propertyName) {
        return this.getTypedProperty(propertyName);
    }

    public BigDecimal getIntegerProperty(String propertyName) {
        return this.getTypedProperty(propertyName);
    }

    public BigDecimal getIntegerProperty(String key, BigDecimal defaultValue) {
        return this.getTypedProperty(key, defaultValue);
    }

    /**
     * Returns the set of property names that are defined at this level,
     * therefore excluding the names of properties that are inherited
     * from a parent level.
     *
     * @return The set of properties that are defined at this level
     */
    public Set<String> localPropertyNames () {
        return new HashSet<>(this.props.keySet());
    }

    /**
     * Returns the set of property names for which a value is available,
     * therefore including the names of properties that are defined
     * at this level or inherited from a parent level.
     *
     * @return The set of properties that are defined at this level
     */
    public Set<String> propertyNames () {
        Set<String> allKeys = this.localPropertyNames();
        if (this.inheritedProperties != null) {
            allKeys.addAll(this.inheritedProperties.propertyNames());
        }
        return allKeys;
    }

    /**
     * Tests if there is a value for the property with the specified name
     * defined on this level.
     *
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    public boolean hasLocalValueFor (String propertyName) {
        return this.props.containsKey(propertyName);
    }

    /**
     * Tests if the value for the property with the specified name
     * is specified on this level.
     *
     * @param value The value
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    public boolean isLocalValueFor (Object value, String propertyName) {
        Object localValue = this.props.get(propertyName);
        return localValue != null && localValue.equals(value);
    }

    /**
     * Tests if there is a value for the property with the specified name
     * inherited from a parent level.
     *
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    public boolean hasInheritedValueFor (String propertyName) {
        return this.inheritedProperties != null && this.inheritedProperties.hasValueFor(propertyName);
    }

    /**
     * Tests if the value for the property with the specified name
     * is specified on the parent level.
     *
     * @param value The value
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    public boolean isInheritedValueFor (Object value, String propertyName) {
        return this.inheritedProperties != null && this.inheritedProperties.isValueFor(value, propertyName);
    }

    /**
     * Tests if there is a value for the property with the specified name
     * defined on this level or inherited from the parent level.
     *
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    public boolean hasValueFor (String propertyName) {
        return this.hasLocalValueFor(propertyName) || this.hasInheritedValueFor(propertyName);
    }

    /**
     * Tests if there is a value for the property with the specified name
     * defined on this level or inherited from the parent level.
     *
     * @param value The value
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    public boolean isValueFor (Object value, String propertyName) {
        return this.isLocalValueFor(value, propertyName) || this.isInheritedValueFor(value, propertyName);
    }

    public Properties toStringProperties() {
        Properties newProps = new Properties();
        for (String propertyName : this.localPropertyNames()) {
            Object value = this.getProperty(propertyName);
            if (value instanceof Boolean) {
                Boolean flag = (Boolean) value;
                newProps.setProperty(propertyName, flag ? "1" : "0");
            }
            else  {
                newProps.setProperty(propertyName, String.valueOf(value));
            }
        }
        return newProps;
    }

    @Override
    public String toString () {
        return this.toStringProperties().toString();
    }

    @Override
    public int hashCode() {
        return props.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        else if (other == null || getClass() != other.getClass()) {
            return false;
        }
        else {
            return this.equals((TypedProperties)other);
        }
    }

    private boolean equals (TypedProperties other) {
        return other.props.equals(this.props);
    }

    /**
     * Method for backwards compatibility, mainly when importing master data.
     * Use {@link #setProperty(String, Object)} instead.
     *
     * @param key key of the property
     * @param value value for the property
     * @deprecated use setProperty(String, Object) instead
     */
    @Deprecated
    public void put(String key, String value) {
        setProperty(key, value);
    }

    public TypedProperties getInheritedProperties () {
        return inheritedProperties;
    }

    public void setInheritedProperties (TypedProperties inheritedProperties) {
        this.inheritedProperties = inheritedProperties;
    }

    public void doCopy(TypedProperties source) {
        this.props = new HashMap<>();
        for (String key : source.props.keySet()) {
            this.props.put(key, source.props.get(key));
        }
        if (source.getInheritedProperties()!=null) {
            this.inheritedProperties = TypedProperties.empty();
            this.inheritedProperties.doCopy(source.getInheritedProperties());
        }
    }

    public TypedProperties getUnmodifiableView() {
        if (unmodifiableView == null) {
            unmodifiableView = new UnmodifiableTypedProperties(this);
        }
        return unmodifiableView;
    }

}