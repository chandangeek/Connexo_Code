/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDateSearchableProperty<T> extends AbstractSearchableDeviceProperty {
    private Class<T> implClass;
    private Thesaurus thesaurus;
    private PropertySpecService propertySpecService;
    private DeviceSearchDomain domain;
    private SearchablePropertyGroup propertyGroup;

    public AbstractDateSearchableProperty(Class<T> clazz, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.implClass = clazz;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    T init(DeviceSearchDomain domain, SearchablePropertyGroup parent) {
        this.domain = domain;
        this.propertyGroup = parent;
        return this.implClass.cast(this);
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return false;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return null;
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
        return Optional.of(this.propertyGroup);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new InstantFactory())
                .named(getNameTranslationKey())
                .fromThesaurus(this.thesaurus)
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    protected Thesaurus getThesaurus() {
        return this.thesaurus;
    }
}
