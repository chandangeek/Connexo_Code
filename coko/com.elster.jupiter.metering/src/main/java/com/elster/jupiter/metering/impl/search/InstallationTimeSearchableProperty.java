/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Where;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InstallationTimeSearchableProperty implements SearchableUsagePointProperty {

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private static final String FIELD_NAME = "installationTime";

    public InstallationTimeSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public SearchDomain getDomain() {
        return domain;
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
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new InstantFactory())
                .named(FIELD_NAME, PropertyTranslationKeys.USAGEPOINT_INSTALLATION_TIME)
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
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_INSTALLATION_TIME.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof Instant) {
            return value.toString();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
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
        if (((Comparison) specification).getOperator().equals(Operator.BETWEEN)) {
            return Where.where(FIELD_NAME)
                    .between(((Comparison) specification).getValues()[0])
                    .and(((Comparison) specification).getValues()[1]);
        }
        return Where.where(FIELD_NAME).isEqualTo((((Comparison) specification).getValues()[0]));
    }

}
