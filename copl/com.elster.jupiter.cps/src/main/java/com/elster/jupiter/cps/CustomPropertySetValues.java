/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CustomAttributeSetProperties model the values for a set of custom properties
 * defined by a {@link CustomPropertySet} and when these values are effective.
 * Each property has a name and a corresponding value.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (16:31)
 */
public final class CustomPropertySetValues {

    private Map<String, Object> values = new HashMap<>();
    private Range<Instant> effective;

    /**
     * Returns a new empty CustomAttributeSetProperties
     * that is always effective.
     *
     * @return The empty CustomAttributeSetProperties
     */
    public static CustomPropertySetValues empty () {
        return new CustomPropertySetValues();
    }

    /**
     * Returns a new empty CustomAttributeSetProperties
     * that is effective from the specified instant in time
     * until the end of time.
     *
     * @param effectiveTimestamp The instant in time when the new CustomPropertySetValues are effective
     * @return The empty CustomAttributeSetProperties
     */
    public static CustomPropertySetValues emptyFrom (Instant effectiveTimestamp) {
        return new CustomPropertySetValues(Range.atLeast(effectiveTimestamp));
    }

    /**
     * Returns a new empty CustomAttributeSetProperties
     * that is effective during the specified Interval.
     *
     * @param interval The Interval during which the new CustomPropertySetValues are effective
     * @return The empty CustomAttributeSetProperties
     */
    public static CustomPropertySetValues emptyDuring (Interval interval) {
        return new CustomPropertySetValues(interval.toClosedOpenRange());
    }

    /**
     * Returns a new empty CustomAttributeSetProperties
     * that is effective during the specified Interval.
     *
     * @param closedOpenRange The Interval during which the new CustomPropertySetValues are effective
     * @return The empty CustomAttributeSetProperties
     */
    public static CustomPropertySetValues emptyDuring (Range<Instant> closedOpenRange) {
        if (!closedOpenRange.hasLowerBound()) {
            throw new IllegalArgumentException("Range must be closed-open or closed");
        }
        return new CustomPropertySetValues(closedOpenRange);
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
        copy.effective = other.effective;
        return copy;
    }

    private CustomPropertySetValues() {
        this(Interval.sinceEpoch().toClosedOpenRange());
    }

    private CustomPropertySetValues(Range<Instant> effective) {
        super();
        this.effective = effective;
    }

    /**
     * Gets the Ranges during which this set of property values is effective.
     *
     * @return The Range
     */
    public Range<Instant> getEffectiveRange() {
        return this.effective;
    }

    /**
     * Tests if this set of property values is effective at the specified instant in time.
     *
     * @param instant The instant in time
     * @return A flag that indicates if this set of property values is effective
     */
    public boolean isEffectiveAt(Instant instant) {
        return this.getEffectiveRange().contains(instant);
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
        return this.values.isEmpty();
    }

    /**
     * Returns the number of properties that are defined.
     *
     * @return The number of properties that are defined
     */
    public int size () {
        return this.values.size();
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
        int result = values.hashCode();
        result = 31 * result + effective.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other != null
                && getClass() == other.getClass()
                && ((CustomPropertySetValues) other).values.equals(this.values)
                && ((CustomPropertySetValues) other).getEffectiveRange().equals(this.getEffectiveRange());
    }
}
