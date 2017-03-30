/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.EnumFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class PhaseCodeSearchableProperty implements SearchableUsagePointProperty {

    private final PropertySpecService propertySpecService;
    private final MeteringTranslationService meteringTranslationService;
    private final Thesaurus thesaurus;

    private SearchablePropertyGroup group;
    private SearchDomain domain;
    private Clock clock;
    private static final String FIELD_NAME = "detail.phaseCode";

    @Inject
    PhaseCodeSearchableProperty(PropertySpecService propertySpecService, MeteringTranslationService meteringTranslationService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.meteringTranslationService = meteringTranslationService;
        this.thesaurus = thesaurus;
    }

    PhaseCodeSearchableProperty init(SearchDomain domain, SearchablePropertyGroup group, Clock clock) {
        this.domain = domain;
        this.group = group;
        this.clock = clock;
        return this;
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
        return Optional.of(this.group);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new EnumFactory(PhaseCode.class))
                .named(FIELD_NAME, PropertyTranslationKeys.USAGEPOINT_PHASECODE)
                .fromThesaurus(this.thesaurus)
                .addValues(PhaseCode.values())
                .markExhaustive()
                .finish();
    }

    @Override
    public SearchableProperty.Visibility getVisibility() {
        return SearchableProperty.Visibility.REMOVABLE;
    }

    @Override
    public SearchableProperty.SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_PHASECODE.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof PhaseCode) {
            return ((PhaseCode) value).getValue();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(new ServiceCategorySearchableProperty(this.domain, this.propertySpecService, meteringTranslationService, this.thesaurus));
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {

    }

    @Override
    public Condition toCondition(Condition specification) {
        return specification.and(Where.where("detail.interval").isEffective(this.clock.instant()));
    }
}
