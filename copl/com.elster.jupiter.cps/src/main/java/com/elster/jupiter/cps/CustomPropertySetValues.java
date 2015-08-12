package com.elster.jupiter.cps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CustomAttributeSetProperties model the values for a set of custom properties
 * defined by a {@link CustomPropertySet}.
 * Each property has a name and a corresponding value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (16:31)
 */
public final class CustomPropertySetValues {

    private Map<String, Object> values = new HashMap<>();

    /**
     * Returns a new empty CustomAttributeSetProperties.
     *
     * @return The empty CustomAttributeSetProperties
     */
    public static CustomPropertySetValues empty () {
        return new CustomPropertySetValues();
    }

    /**
     * Creates a new CustomAttributeSetProperties that is an exact copy
     * of the specified {@link CustomPropertySetValues}.
     *
     * @param other The other CustomAttributeSetProperties
     * @return The copy of the CustomAttributeSetProperties
     */
    public static CustomPropertySetValues copyOf (CustomPropertySetValues other) {
        CustomPropertySetValues copy = CustomPropertySetValues.empty();
        copy.values.putAll(other.values);
        return copy;
    }

    private CustomPropertySetValues() {
        super();
    }

    /**
     * Gets the value for the property with the specified name.
     *
     * @param propertyName The name of the property
     * @return The value
     */
    public Object getProperty(String propertyName) {
        return this.values.get(propertyName);
    }

    /**
     * Set a value for the property with the specified name.
     *
     * @param propertyName The name of the property for which a value is set
     * @param value The value
     * @see #removeProperty(String)
     */
    public void setProperty(String propertyName, Object value) {
        this.values.put(propertyName, value);
    }

    /**
     * Removes the value of the property with the specified name.
     *
     * @param propertyName The name of the property for which the value is to be removed
     */
    public void removeProperty(String propertyName) {
        this.values.remove(propertyName);
    }

    /**
     * Tests if this set of values is empty.
     * A set is empty if no properties were set.
     * As soon as the {@link #setProperty(String, Object)} method
     * is called once, even if the value of the property is <code>null</code>
     * the set is no longer empty.
     *
     * @return A flag that indicates if there are properties in this set
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Returns the number of properties that are defined.
     *
     * @return The number of properties that are defined
     */
    public int size () {
        return this.propertyNames().size();
    }

    /**
     * Returns the set of property names for which a value is available.
     *
     * @return The set of properties that are defined
     */
    public Set<String> propertyNames () {
        return new HashSet<>(this.values.keySet());
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
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
            return ((CustomPropertySetValues) other).values.equals(this.values);
        }
    }

}