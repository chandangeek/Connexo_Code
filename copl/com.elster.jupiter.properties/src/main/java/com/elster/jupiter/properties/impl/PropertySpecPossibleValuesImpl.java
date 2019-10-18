/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpecPossibleValues;

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
public class PropertySpecPossibleValuesImpl implements PropertySpecPossibleValues, Serializable {

    private List allValues = new ArrayList<>();
    private Object defaultValue;
    private boolean exhaustive = false;
    private boolean editable = false;
    private PropertySelectionMode selectionMode = PropertySelectionMode.UNSPECIFIED;

    public PropertySpecPossibleValuesImpl () {
        super();
    }

    public PropertySpecPossibleValuesImpl (boolean exhaustive, Collection allValues) {
        this.defaultValue = null;
        this.exhaustive = exhaustive;
        this.allValues = this.copyUniqueWithRespectForOrder(allValues, allValues.size());
    }

    @SuppressWarnings("unchecked")
    private List copyUniqueWithRespectForOrder (Iterable allValues, int numberOfValues) {
        List copied = new ArrayList(numberOfValues);   // At worst, the values are all unique and we need an ArrayList of the same size
        Set uniqueValues = new HashSet();
        for (Object value : allValues) {
            if (uniqueValues.add(value)) {
                // Not in set yet
                copied.add(value);
            }
        }
        return copied;
    }

    public PropertySpecPossibleValuesImpl (Object defaultValue, boolean exhaustive, Object... otherValues) {
        this(defaultValue, exhaustive, Arrays.asList(otherValues));
    }

    @SuppressWarnings("unchecked")
    public PropertySpecPossibleValuesImpl (Object defaultValue, boolean exhaustive, Collection otherValues) {
        this();
        this.defaultValue = defaultValue;
        this.exhaustive = exhaustive;
        this.allValues.addAll(this.copyUniqueWithRespectForOrder(otherValues, otherValues.size()));
    }

    @Override
    public PropertySelectionMode getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(PropertySelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List getAllValues () {
        return new ArrayList(this.allValues);
    }

    @Override
    public boolean isExhaustive () {
        return this.exhaustive;
    }

    // Allow friendly builder components to change the exhaustive flag after construction
    public void setExhaustive (boolean exhaustive) {
        this.exhaustive = exhaustive;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public Object getDefault () {
        return this.defaultValue;
    }

    // Allow friendly builder components to overrule the default
    public void setDefault (Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @SuppressWarnings("unchecked")
    public void add (Object... values) {
        this.allValues.addAll(this.copyUniqueWithRespectForOrder(Arrays.asList(values), values.length));
    }

}