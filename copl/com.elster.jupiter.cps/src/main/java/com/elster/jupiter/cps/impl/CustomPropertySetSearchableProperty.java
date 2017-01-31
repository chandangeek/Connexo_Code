/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class CustomPropertySetSearchableProperty implements SearchableProperty {

    private final CustomPropertySet<?, ?> customPropertySet;
    private final SearchablePropertyGroup searchablePropertyGroup;
    private final PropertySpec propertySpec;
    private final List<SearchableProperty> constraints;

    CustomPropertySetSearchableProperty(CustomPropertySet<?, ?> customPropertySet, PropertySpec propertySpec, SearchablePropertyGroup searchablePropertyGroup, List<SearchableProperty> constraints) {
        this.customPropertySet = customPropertySet;
        this.propertySpec = propertySpec;
        this.searchablePropertyGroup = searchablePropertyGroup;
        this.constraints = Collections.unmodifiableList(constraints);
    }

    @Override
    public SearchDomain getDomain() {
        return null; // a search domain wrapper will provide a correct value
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(this.searchablePropertyGroup);
    }

    @Override
    public String getName() {
        return this.customPropertySet.getId() + "." + this.propertySpec.getName();
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpec;
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        PropertySpecPossibleValues possibleValues = this.propertySpec.getPossibleValues();
        if (possibleValues != null && !possibleValues.getAllValues().isEmpty()) {
            return SelectionMode.MULTI;
        }
        return SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        return this.propertySpec.getDisplayName();
    }

    @Override
    public String toDisplay(Object value) {
        return String.valueOf(value);
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return this.constraints;
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // do nothing
    }
}
