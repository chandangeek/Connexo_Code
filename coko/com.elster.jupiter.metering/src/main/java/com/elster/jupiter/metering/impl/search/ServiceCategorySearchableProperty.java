/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.EnumFactory;
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

/**
 * Exposes the service kind enumeration
 * of a {@link com.elster.jupiter.metering.UsagePoint}
 * as a {@link SearchableProperty}.
 *
 * @author Anton Fomchenko
 * @since 2015-08-12
 */
public class ServiceCategorySearchableProperty implements SearchableUsagePointProperty {

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final MeteringTranslationService meteringTranslationService;
    private final Thesaurus thesaurus;
    static final String FIELD_NAME = "SERVICEKIND";

    public ServiceCategorySearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, MeteringTranslationService meteringTranslationService, Thesaurus thesaurus) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.meteringTranslationService = meteringTranslationService;
        this.thesaurus = thesaurus;
    }

    @Override
    public SearchDomain getDomain() {
        return domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return true;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.empty();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.STICKY;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_SERVICECATEGORY.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof ServiceKind) {
            ServiceKind serviceKind = (ServiceKind) value;
            return this.meteringTranslationService.getDisplayName(serviceKind);
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new EnumFactory(ServiceKind.class))
                .named(FIELD_NAME, PropertyTranslationKeys.USAGEPOINT_SERVICECATEGORY)
                .fromThesaurus(this.thesaurus)
                .addValues(ServiceKind.ELECTRICITY,
                        ServiceKind.GAS,
                        ServiceKind.HEAT,
                        ServiceKind.WATER)
                .markExhaustive()
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    @Override
    public Condition toCondition(Condition specification) {
        return specification;
    }

}