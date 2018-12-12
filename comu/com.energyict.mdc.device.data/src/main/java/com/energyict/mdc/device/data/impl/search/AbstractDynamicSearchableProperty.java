/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDynamicSearchableProperty<T> extends AbstractSearchableDeviceProperty {

    private final Class<T> implClass;
    private SearchDomain domain;
    private SearchablePropertyGroup group;
    private PropertySpec propertySpec;
    private SearchableProperty constraintProperty;

    public AbstractDynamicSearchableProperty(Class<T> implClass, Thesaurus thesaurus) {
        super(thesaurus);
        this.implClass = implClass;
    }

    public T init(SearchDomain domain, SearchablePropertyGroup group, PropertySpec propertySpec, SearchableProperty constraintProperty) {
        this.domain = domain;
        this.group = group;
        this.propertySpec = propertySpec;
        this.constraintProperty = constraintProperty;
        return this.implClass.cast(this);
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(this.group);
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        if (this.propertySpec.getPossibleValues() != null){
            List allValues = propertySpec.getPossibleValues().getAllValues();
            if (allValues != null && !allValues.isEmpty()){
                return SelectionMode.MULTI;
            }
        }
        return SelectionMode.SINGLE;
    }

    @Override
    public SearchDomain getDomain() {
        return this.domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public String getName() {
        return this.group.getId() + "." + this.propertySpec.getName();
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpec;
    }

    @Override
    public String getDisplayName() {
        return this.propertySpec.getDisplayName();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(this.constraintProperty);
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // Nothing to refresh
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        try {
            this.propertySpec.validateValueIgnoreRequired(value);
            return true;
        } catch (InvalidValueException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String toDisplayAfterValidation(Object value) {
        return this.propertySpec.getValueFactory().toStringValue(value);
    }

    public PropertySpec getPropertySpec() {
        return propertySpec;
    }
}