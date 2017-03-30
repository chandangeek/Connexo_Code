/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UsagePointIdSearchableProperty implements SearchableUsagePointProperty {
    private static final String FIELD_NAME = "id";

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;

    public UsagePointIdSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public Condition toCondition(Condition specification) {
        return specification;
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
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.empty();
    }

    @Override
    public String getName() {
        return FIELD_NAME;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .longSpec()
                .named(FIELD_NAME, "")
                .describedAs("")
                .markRequired()
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        throw unsupported();
    }

    @Override
    public String toDisplay(Object value) {
        throw unsupported();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        // do nothing
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("This method is not supported for that searchable property: " + getClass().getName());
    }
}
