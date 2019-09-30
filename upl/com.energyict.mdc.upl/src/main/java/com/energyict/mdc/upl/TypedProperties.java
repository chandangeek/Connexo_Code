/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

/**
 * TypedProperties model a set of dynamic properties.
 * Each property has a name (aka the key) and a corresponding value.
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
@XmlRootElement
public class TypedProperties implements com.energyict.mdc.upl.properties.TypedProperties {

    private Map<String, Object> props = new HashMap<>();
    private com.energyict.mdc.upl.properties.TypedProperties inheritedProperties;

    protected TypedProperties() {
        super();
    }

    /**
     * Returns a new empty TypedProperties.
     *
     * @return The empty TypedProperties
     */
    public static TypedProperties empty() {
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
    public static TypedProperties inheritingFrom(com.energyict.mdc.upl.properties.TypedProperties inheritedProperties) {
        TypedProperties typedProperties = empty();
        if (inheritedProperties == null) {
            typedProperties.inheritedProperties = null;
        } else {
            typedProperties.inheritedProperties = TypedProperties.copyOf(inheritedProperties);
        }
        return typedProperties;
    }

    /**
     * Creates a new TypeProperties object that is an exact copy
     * of the specified {@link TypedProperties}.
     *
     * @param other The other TypedProperties
     * @return The copy of the TypedProperties
     */
    public static TypedProperties copyOf(com.energyict.mdc.upl.properties.TypedProperties other) {
        TypedProperties result;
        if (other.getInheritedProperties() == null) {
            result = empty();
        } else {
            result = inheritingFrom(other.getInheritedProperties());
        }
        result.setAllProperties(other);
        return result;
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
    public static TypedProperties copyOf(Properties simpleProperties) {
        TypedProperties typedProperties = empty();
        for (Object key : simpleProperties.keySet()) {
            typedProperties.setProperty((String) key, simpleProperties.getProperty((String) key));
        }
        return typedProperties;
    }

    /**
     * Set a value for the property with the specified name.
     * Note that the value that was inherited will be overrule
     * by the one that is set now. It will remain overruled until
     * the value of the property is removed.
     *
     * @param propertyName The name of the property for which a value is set
     * @param value        The value
     * @see #removeProperty(String)
     */
    @Override
    public void setProperty(String propertyName, Object value) {
        this.props.put(propertyName, value);
    }

    @Override
    public void setAllLocalProperties(com.energyict.mdc.upl.properties.TypedProperties otherTypedProperties) {
        this.setAllProperties(otherTypedProperties, false);
    }

    @Override
    public void setAllProperties(com.energyict.mdc.upl.properties.TypedProperties otherTypedProperties) {
        this.setAllProperties(otherTypedProperties, true);
    }

    /**
     * Sets all the properties that are defined by the other
     * TypedProperties, and optionally from its inherited values).
     *
     * @param otherTypedProperties       The other TypedProperties from which value are copied
     * @param includeInheritedProperties boolean indicating whether or not the inherited values should be set as well -
     *                                   if true, the inherited properties are added <b>as local</b> property of this instance
     */
    public void setAllProperties(com.energyict.mdc.upl.properties.TypedProperties otherTypedProperties, boolean includeInheritedProperties) {
        /* If needed, first we add the inherited properties, then the local (so we can overwrite the inherited) */
        if (includeInheritedProperties && otherTypedProperties.getInheritedProperties() != null) {
            this.setAllProperties(otherTypedProperties.getInheritedProperties(), true);
        }

        //Now add the local properties
        otherTypedProperties
                .localPropertyNames()
                .forEach(name -> this.props.put(name, otherTypedProperties.getProperty(name)));
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
            } else {
                return null;
            }
        } else {
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
    public <T> T getTypedProperty(String propertyName) {
        Object valueFromThisLevel = this.props.get(propertyName);
        if (valueFromThisLevel == null) {
            if (this.inheritedProperties != null) {
                return this.inheritedProperties.getTypedProperty(propertyName);
            } else {
                return null;
            }
        } else {
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
     * or the default value when no level provides a value
     */
    public Object getProperty(String propertyName, Object defaultValue) {
        Object valueFromThisLevel = this.props.get(propertyName);
        if (valueFromThisLevel == null) {
            if (this.inheritedProperties != null) {
                return this.inheritedProperties.getProperty(propertyName, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
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
     * or the default value when no level provides a value
     */
    public <T> T getTypedProperty(String propertyName, T defaultValue) {
        Object valueFromThisLevel = this.props.get(propertyName);
        if (valueFromThisLevel == null) {
            if (this.inheritedProperties != null) {
                return this.inheritedProperties.getTypedProperty(propertyName, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
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
    public int localSize() {
        return props.size();
    }

    /**
     * Returns the number of properties that are defined,
     * including the number of properties that are inherited from the parent level.
     *
     * @return The number of properties that are defined
     */
    public int size() {
        return this.propertyNames().size();
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
    public Set<String> localPropertyNames() {
        return new HashSet<>(this.props.keySet());
    }

    /**
     * Returns the set of property names for which a value is available,
     * therefore including the names of properties that are defined
     * at this level or inherited from a parent level.
     *
     * @return The set of properties that are defined at this level
     */
    public Set<String> propertyNames() {
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
    public boolean hasLocalValueFor(String propertyName) {
        return this.props.containsKey(propertyName);
    }

    /**
     * Tests if the value for the property with the specified name
     * is specified on this level.
     *
     * @param value        The value
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    public boolean isLocalValueFor(Object value, String propertyName) {
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
    public boolean hasInheritedValueFor(String propertyName) {
        return this.inheritedProperties != null && this.inheritedProperties.hasValueFor(propertyName);
    }

    /**
     * Tests if the value for the property with the specified name
     * is specified on the parent level.
     *
     * @param value        The value
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    public boolean isInheritedValueFor(Object value, String propertyName) {
        return this.inheritedProperties != null && this.inheritedProperties.isValueFor(value, propertyName);
    }

    /**
     * Tests if there is a value for the property with the specified name
     * defined on this level or inherited from the parent level.
     *
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    public boolean hasValueFor(String propertyName) {
        return this.hasLocalValueFor(propertyName) || this.hasInheritedValueFor(propertyName);
    }

    /**
     * Tests if there is a value for the property with the specified name
     * defined on this level or inherited from the parent level.
     *
     * @param value        The value
     * @param propertyName The name of the property
     * @return A flag that indicates if there is a value for the property
     */
    public boolean isValueFor(Object value, String propertyName) {
        return this.isLocalValueFor(value, propertyName) || this.isInheritedValueFor(value, propertyName);
    }

    /**
     * Return a list of all properties (inlcuding the inherited ones) in string format
     */
    public Properties toStringProperties() {
        Properties newProps = new Properties();
        for (String propertyName : this.propertyNames()) {
            Object value = this.getProperty(propertyName);
            if (value instanceof Boolean) {
                Boolean flag = (Boolean) value;
                newProps.setProperty(propertyName, flag ? "1" : "0");
            } else if (value instanceof Duration && "Timeout".equalsIgnoreCase(propertyName)) {
                newProps.setProperty(propertyName, String.valueOf(((Duration) value).getSeconds() * 1000));
            } else if (value != null) {
                newProps.setProperty(propertyName, String.valueOf(value));
            }
        }
        return newProps;
    }


    @Override
    public String toString() {
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
        } else if (other == null || getClass() != other.getClass()) {
            return false;
        } else {
            return ((TypedProperties) other).props.equals(this.props);
        }
    }

    // Added for serializability
    @XmlAttribute
    public Map<String, Object> getHashMap() {
        return Collections.unmodifiableMap(props);
    }

    public void setHashMap(Map<String, Object> props) {
        this.props = new HashMap<>(props);
    }

    @XmlAttribute
    @Override
    public com.energyict.mdc.upl.properties.TypedProperties getInheritedProperties() {
        return inheritedProperties;
    }

    public void setInheritedProperties(TypedProperties inheritedProperties) {
        this.inheritedProperties = inheritedProperties;
    }

    /**
     * Gets the value for the property with the specified name
     * that is defined at this level (no inherited value from
     * a parent level).
     *
     * @param propertyName The name of the property
     * @return The value defined at this level
     */
    public Object getLocalValue(String propertyName) {
        return this.props.get(propertyName);
    }

    /**
     * Gets the value for the property with the specified name
     * that is defined at a higher level.
     *
     * @param propertyName The name of the property
     * @return The value defined at a higher level or <code>null</code> if there is no inherited value
     */
    public Object getInheritedValue(String propertyName) {
        if (this.inheritedProperties != null) {
            return this.inheritedProperties.getProperty(propertyName);
        } else {
            return null;
        }
    }

    /**
     * Getter to be used in XML (de)marshalling only!
     * <br></br>
     * Getter will return a HashTable containing <i>property name - property value class name</i> pairs.
     */
    @XmlAttribute
    public Hashtable<String, String> getPropertyKeyPropertyClassMap() {
        Hashtable<String, String> map = new Hashtable<>();
        for (Object o : props.entrySet()) {
            Map.Entry pairs = (Map.Entry) o;
            String key = (String) pairs.getKey();
            Object value = pairs.getValue();
            map.put(key, value.getClass().getName());
        }
        return map;
    }

    private void setPropertyKeyPropertyClassMap(Hashtable<String, String> hashTable) {
        // For xml unmarshalling purposes only
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    /**
     * Returns a stream of Map.Entry elements, containing inherited properties and local properties (local overwrites inherited)
     * @return
     */
    public Stream<Map.Entry<String, Object>> stream() {
        Map<String, Object> properties = this.inheritedProperties!=null?new HashMap<>(((TypedProperties) this.inheritedProperties).getHashMap()):new HashMap<>();
        properties.putAll(this.getHashMap());
        return properties.entrySet().stream();
    }

}