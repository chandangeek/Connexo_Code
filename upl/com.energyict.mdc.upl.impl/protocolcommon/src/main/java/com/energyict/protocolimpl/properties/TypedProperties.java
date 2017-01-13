package com.energyict.protocolimpl.properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Provides an implementation for the {<link com.energyict.mdc.upl.properties.TypedProperties} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-24 (14:36)
 */
public class TypedProperties implements com.energyict.mdc.upl.properties.TypedProperties {

    private Map<String, Object> props = new HashMap<>();
    private TypedProperties inheritedProperties;

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
    public static TypedProperties inheritingFrom (com.energyict.mdc.upl.properties.TypedProperties inheritedProperties) {
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
    public static TypedProperties copyOf (com.energyict.mdc.upl.properties.TypedProperties other) {
        TypedProperties typedProperties = TypedProperties.inheritingFrom(other.getInheritedProperties());
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
     * by the one that is set now. It will remain overrule until
     * the value of the property is removed.
     *
     * @param propertyName The name of the property for which a value is set
     * @param value The value
     * @see #removeProperty(String)
     */
    @Override
    public void setProperty(String propertyName, Object value) {
        this.props.put(propertyName, value);
    }

    @Override
    public void setAllLocalProperties(com.energyict.mdc.upl.properties.TypedProperties otherTypedProperties) {
        if (otherTypedProperties instanceof TypedProperties) {
            TypedProperties other = (TypedProperties) otherTypedProperties;
            this.setAllProperties(other, false);
        } else {
            throw new IllegalArgumentException("Expected instance of " + this.getClass().getName());
        }
    }

    @Override
    public void setAllProperties (com.energyict.mdc.upl.properties.TypedProperties otherTypedProperties) {
        if (otherTypedProperties instanceof TypedProperties) {
            TypedProperties other = (TypedProperties) otherTypedProperties;
            this.setAllProperties(other, true);
        } else {
            otherTypedProperties
                    .propertyNames()
                    .forEach(propertyName -> this.setOneProperty(otherTypedProperties, propertyName));
        }
    }

    private void setOneProperty(com.energyict.mdc.upl.properties.TypedProperties other, String propertyName) {
        this.props.put(propertyName, other.getProperty(propertyName));
    }

    /**
     * Sets all the properties that are defined by the other
     * TypedProperties, and optionally from its inherited values).
     *
     * @param otherTypedProperties The other TypedProperties from which value are copied
     * @param includeInheritedProperties boolean indicating whether or not the inherited values should be set as well -
     *                                   if true, the inherited properties are added <b>as local</b> property of this instance
     */
    private void setAllProperties (TypedProperties otherTypedProperties, boolean includeInheritedProperties) {
        /* If needed, first we add the inherited properties, then the local (so we can overwrite the inherited) */
        if (includeInheritedProperties && otherTypedProperties.getInheritedProperties() != null) {
            this.setAllProperties(otherTypedProperties.getInheritedProperties(), true);
        }
        this.props.putAll(otherTypedProperties.props);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void removeProperty(String propertyName) {
        this.props.remove(propertyName);
    }

    @Override
    public TypedProperties clone() {
        return TypedProperties.copyOf(this);
    }

    @Override
    public int localSize () {
        return props.size();
    }

    @Override
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

    @Override
    public Set<String> localPropertyNames () {
        return new HashSet<>(this.props.keySet());
    }

    @Override
    public Set<String> propertyNames () {
        Set<String> allKeys = this.localPropertyNames();
        if (this.inheritedProperties != null) {
            allKeys.addAll(this.inheritedProperties.propertyNames());
        }
        return allKeys;
    }

    @Override
    public boolean hasLocalValueFor (String propertyName) {
        return this.props.containsKey(propertyName);
    }

    @Override
    public boolean isLocalValueFor (Object value, String propertyName) {
        Object localValue = this.props.get(propertyName);
        return localValue != null && localValue.equals(value);
    }

    @Override
    public boolean hasInheritedValueFor (String propertyName) {
        return this.inheritedProperties != null && this.inheritedProperties.hasValueFor(propertyName);
    }

    @Override
    public boolean isInheritedValueFor (Object value, String propertyName) {
        return this.inheritedProperties != null && this.inheritedProperties.isValueFor(value, propertyName);
    }

    @Override
    public boolean hasValueFor (String propertyName) {
        return this.hasLocalValueFor(propertyName) || this.hasInheritedValueFor(propertyName);
    }

    @Override
    public boolean isValueFor (Object value, String propertyName) {
        return this.isLocalValueFor(value, propertyName) || this.isInheritedValueFor(value, propertyName);
    }

    @Override
    public Properties toStringProperties() {
        Properties newProps = new Properties();
        for (String propertyName : this.propertyNames()) {
            Object value = this.getProperty(propertyName);
            if (value instanceof Boolean) {
                Boolean flag = (Boolean) value;
                newProps.setProperty(propertyName, flag ? "1" : "0");
            } else if (value instanceof Duration && "Timeout".equalsIgnoreCase(propertyName)){
                newProps.setProperty(propertyName, String.valueOf(((Duration) value).getSeconds() * 1000));
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
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return ((TypedProperties)other).getHashMap().equals(props);
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

// Added for serializability

    @XmlAttribute
    public Map<String, Object> getHashMap() {
        return props;
    }

    public void setHashMap(Map<String, Object> props) {
        this.props = props;
    }

    @XmlAttribute
    @Override
    public TypedProperties getInheritedProperties () {
        return inheritedProperties;
    }

    public void setInheritedProperties (TypedProperties inheritedProperties) {
        this.inheritedProperties = inheritedProperties;
    }

    /**
     * Getter to be used in XML (de)marshalling only!
     * <br></br>
     * Getter will return a HashTable containing <i>&#60;property name - property value class name&#62;</i> pairs.
     */
    @XmlAttribute
    public Hashtable<String, String> getPropertyKeyPropertyClassMap() {
        Hashtable<String, String> map = new Hashtable<>();
        Iterator it = props.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pairs = (Map.Entry) it.next();
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

}