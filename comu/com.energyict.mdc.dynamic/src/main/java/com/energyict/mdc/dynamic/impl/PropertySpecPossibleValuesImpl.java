package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.dynamic.PropertySpecPossibleValues;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link PropertySpecPossibleValues}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-19 (16:15)
 */
public class PropertySpecPossibleValuesImpl<T> implements PropertySpecPossibleValues<T>, Serializable {

    private List<T> allValues = new ArrayList<>();
    private T defaultValue;
    private boolean exhaustive = false;

    public PropertySpecPossibleValuesImpl () {
        super();
    }

    public PropertySpecPossibleValuesImpl (boolean exhaustive, Collection<T> allValues) {
        this.defaultValue = null;
        this.exhaustive = exhaustive;
        this.allValues = this.copyUniqueWithRespectForOrder(allValues, allValues.size());
    }

    private List<T> copyUniqueWithRespectForOrder (Iterable<T> allValues, int numberOfValues) {
        List<T> copied = new ArrayList<>(numberOfValues);   // At worst, the values are all unique and we need an ArrayList of the same size
        Set<T> uniqueValues = new HashSet<>();
        for (T value : allValues) {
            if (uniqueValues.add(value)) {
                // Not in set yet
                copied.add(value);
            }
        }
        return copied;
    }

    @SafeVarargs
    public PropertySpecPossibleValuesImpl (T defaultValue, boolean exhaustive, T... otherValues) {
        this(defaultValue, exhaustive, Arrays.asList(otherValues));
    }

    public PropertySpecPossibleValuesImpl (T defaultValue, boolean exhaustive, Collection<T> otherValues) {
        this();
        this.defaultValue = defaultValue;
        this.exhaustive = exhaustive;
        this.allValues.add(defaultValue);
        this.allValues.addAll(this.copyUniqueWithRespectForOrder(otherValues, otherValues.size()));
    }

    @Override
    public List<? super T> getAllValues () {
        return new ArrayList<>(this.allValues);
    }

    @Override
    public boolean isExhaustive () {
        return this.exhaustive;
    }

    // Allow friendly builder components to change the exhaustive flag after construction
    public void setExhaustive (boolean exhaustive) {
        this.exhaustive = exhaustive;
    }

    @Override
    public T getDefault () {
        return this.defaultValue;
    }

    // Allow friendly builder components to overrule the default
    public void setDefault (T defaultValue) {
        this.defaultValue = defaultValue;
        this.allValues.add(defaultValue);
    }

    // Allow friendly builder components to add values
    protected void add (T... values) {
        this.allValues.addAll(this.copyUniqueWithRespectForOrder(Arrays.asList(values), values.length));
    }

}