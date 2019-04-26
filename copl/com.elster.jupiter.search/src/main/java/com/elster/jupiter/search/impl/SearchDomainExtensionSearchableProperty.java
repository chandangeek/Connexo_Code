/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.List;
import java.util.Optional;

class SearchDomainExtensionSearchableProperty implements SearchableProperty {
    private final SearchDomain searchDomain;
    private final SearchDomainExtension searchDomainExtension;
    private final SearchableProperty searchableProperty;

    SearchDomainExtensionSearchableProperty(SearchDomain searchDomain, SearchDomainExtension searchDomainExtension, SearchableProperty searchableProperty) {
        this.searchDomain = searchDomain;
        this.searchDomainExtension = searchDomainExtension;
        this.searchableProperty = searchableProperty;
    }

    @Override
    public SearchDomain getDomain() {
        return searchDomain;
    }

    SearchDomainExtension getDomainExtension() {
        return searchDomainExtension;
    }

    SearchableProperty unwrap() {
        return searchableProperty;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return searchableProperty.affectsAvailableDomainProperties();
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return searchableProperty.getGroup();
    }

    @Override
    public PropertySpec getSpecification() {
        return searchableProperty.getSpecification();
    }

    @Override
    public Visibility getVisibility() {
        return searchableProperty.getVisibility();
    }

    @Override
    public SelectionMode getSelectionMode() {
        return searchableProperty.getSelectionMode();
    }

    @Override
    public String getName() {
        return searchableProperty.getName();
    }

    @Override
    public String getDisplayName() {
        return searchableProperty.getDisplayName();
    }

    @Override
    public String toDisplay(Object value) {
        return searchableProperty.toDisplay(value);
    }

    @Override
    public boolean hasName(String name) {
        return searchableProperty.hasName(name);
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return searchableProperty.getConstraints();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        searchableProperty.refreshWithConstrictions(constrictions);
    }
}
