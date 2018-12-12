package com.energyict.protocolimpl.properties;

import com.energyict.mdc.upl.properties.PropertySelectionMode;
import com.energyict.mdc.upl.properties.PropertySpecPossibleValues;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PropertySpecPossibleValues} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-19 (16:15)
 */
public class PropertySpecPossibleValuesImpl implements PropertySpecPossibleValues, Serializable {

    private transient List allValues = new ArrayList();
    private boolean exhaustive = false;
    private transient Object defaultValue;
    private PropertySelectionMode selectionMode;

    public PropertySpecPossibleValuesImpl() {
        super();
    }

    public PropertySpecPossibleValuesImpl(boolean exhaustive, Collection allValues) {
        this.defaultValue = null;
        this.exhaustive = exhaustive;
        List uniqueValues = this.copyUniqueWithRespectForOrder(allValues);
        setAllValues(uniqueValues);
    }

    private List copyUniqueWithRespectForOrder(Iterable allValues) {
        return Stream.of(allValues).distinct().collect(Collectors.toList());
    }

    public PropertySpecPossibleValuesImpl(Object defaultValue, boolean exhaustive, Object... otherValues) {
        this(defaultValue, exhaustive, Arrays.asList(otherValues));
    }

    public PropertySpecPossibleValuesImpl(Object defaultValue, boolean exhaustive, Collection otherValues) {
        this();
        this.defaultValue = defaultValue;
        this.exhaustive = exhaustive;
        List allIncomingValues = this.copyUniqueWithRespectForOrder(otherValues);
        allIncomingValues.add(defaultValue);
        setAllValues(allIncomingValues);
    }

    @Override
    public PropertySelectionMode getSelectionMode() {
        return this.selectionMode;
    }

    void setSelectionMode(PropertySelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public List getAllValues () {
        if (this.allValues == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(this.allValues);
        }
    }

    // For xml serialization purposes only
    public void setAllValues (List allValues) {
        this.allValues = new ArrayList(allValues);
    }

    @Override
    public boolean isExhaustive () {
        return this.exhaustive;
    }

    // Allow xml serialization mechanism and friendly builder components to change the exhaustive flag after construction
    public void setExhaustive (boolean exhaustive) {
        this.exhaustive = exhaustive;
    }

    @Override
    public Object getDefault () {
        return this.defaultValue;
    }

    // Allow xml serialization mechanism or friendly builder components to overrule the default
    public void setDefault (Object defaultValue) {
        this.defaultValue = defaultValue;
        if (defaultValue != null && !this.allValues.contains(defaultValue)) {
            this.allValues.add(defaultValue);
        }
    }

    // Allow friendly builder components to add values
    protected void add (Object... values) {
        this.allValues.addAll(this.copyUniqueWithRespectForOrder(Arrays.asList(values)));
    }

}