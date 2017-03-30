/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.EnumFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class CollarSearchableProperty implements SearchableUsagePointProperty {

    private final PropertySpecService propertySpecService;
    private final MeteringTranslationService meteringTranslationService;
    private final Thesaurus thesaurus;

    private Clock clock;
    private SearchDomain domain;
    private ServiceKindAwareSearchablePropertyGroup group;
    private static final String FIELD_NAME = "detail.collar";
    private String uniqueName;

    @Inject
    CollarSearchableProperty(PropertySpecService propertySpecService, MeteringTranslationService meteringTranslationService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.meteringTranslationService = meteringTranslationService;
        this.thesaurus = thesaurus;
    }

    CollarSearchableProperty init(SearchDomain searchDomain, ServiceKindAwareSearchablePropertyGroup group, Clock clock) {
        this.domain = searchDomain;
        this.group = group;
        this.uniqueName = FIELD_NAME + "." + group.getId();
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
        return Optional.of(group);
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.MULTI;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_COLLAR.getDisplayName(thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof YesNoAnswer) {
            String string = value.toString();
            return thesaurus.getString(string, string);
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public PropertySpec getSpecification() {
        return propertySpecService
                .specForValuesOf(new EnumFactory(YesNoAnswer.class))
                .named(uniqueName, PropertyTranslationKeys.USAGEPOINT_COLLAR)
                .fromThesaurus(thesaurus)
                .addValues(YesNoAnswer.values())
                .markExhaustive()
                .finish();
    }


    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.singletonList(new ServiceCategorySearchableProperty(domain, propertySpecService, meteringTranslationService, thesaurus));
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    @Override
    public Condition toCondition(Condition specification) {
        Contains condition = (Contains) specification;
        List<Object> values = new ArrayList<>(condition.getCollection());
        return condition.getOperator()
                .contains(FIELD_NAME, values)
                .and(Where.where("detail.interval").isEffective(this.clock.instant()))
                .and(Where.where("serviceCategory.kind")
                        .isEqualTo(this.group.getServiceKind()));
    }
}
