/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class LoadLimiterTypeSearchableProperty implements SearchableUsagePointProperty {

    private final PropertySpecService propertySpecService;
    private final MeteringTranslationService meteringTranslationService;
    private final Thesaurus thesaurus;

    private SearchDomain domain;
    private ServiceKindAwareSearchablePropertyGroup group;
    private Clock clock;
    static final String FIELD_NAME = "detail.loadLimiterType";
    private String uniqueName;

    @Inject
    LoadLimiterTypeSearchableProperty(PropertySpecService propertySpecService, MeteringTranslationService meteringTranslationService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.meteringTranslationService = meteringTranslationService;
        this.thesaurus = thesaurus;
    }

    LoadLimiterTypeSearchableProperty init(SearchDomain domain, ServiceKindAwareSearchablePropertyGroup group, Clock cLock) {
        this.domain = domain;
        this.group = group;
        this.clock = cLock;
        this.uniqueName = FIELD_NAME + "." + group.getId();
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
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_LOAD_LIMITER_TYPE.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .stringSpec()
                .named(uniqueName, PropertyTranslationKeys.USAGEPOINT_LOAD_LIMITER_TYPE)
                .fromThesaurus(this.thesaurus)
                .finish();
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(new LimiterSearchableProperty(this.propertySpecService, meteringTranslationService, this.thesaurus).init(this.domain, this.group, this.clock));
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    @Override
    public Condition toCondition(Condition specification) {
        Comparison condition = (Comparison) specification;
        return condition.getOperator().compare(FIELD_NAME, condition.getValues())
                .and(Where.where("detail.interval").isEffective(this.clock.instant()))
                .and(Where.where("serviceCategory.kind")
                        .isEqualTo(this.group.getServiceKind()));
    }
}
