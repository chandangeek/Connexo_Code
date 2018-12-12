/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractObisCodeSearchableProperty<T> extends AbstractSearchableDeviceProperty {

    private final Class<T> implClass;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private DeviceSearchDomain domain;
    private SearchablePropertyGroup group;

    public AbstractObisCodeSearchableProperty(Class<T> clazz, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.implClass = clazz;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    T init(DeviceSearchDomain domain, SearchablePropertyGroup group) {
        this.domain = domain;
        this.group = group;
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
        return Optional.of(this.group);
    }

    @Override
    public abstract String getName();

    protected abstract TranslationKey getNameTranslationKey();

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .stringSpec()
                .named(this.getNameTranslationKey())
                .fromThesaurus(thesaurus)
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

    protected Thesaurus getThesaurus(){
        return this.thesaurus;
    }
}
