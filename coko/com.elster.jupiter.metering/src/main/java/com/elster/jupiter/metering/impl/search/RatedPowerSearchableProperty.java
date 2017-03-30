/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class RatedPowerSearchableProperty implements SearchableUsagePointProperty {

    private final PropertySpecService propertySpecService;
    private final MeteringTranslationService meteringTranslationService;
    private final Thesaurus thesaurus;

    private SearchDomain domain;
    private SearchablePropertyGroup group;
    private Clock clock;
    private static final String FIELD_NAME = "detail.ratedPower";

    @Inject
    RatedPowerSearchableProperty(PropertySpecService propertySpecService, MeteringTranslationService meteringTranslationService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.meteringTranslationService = meteringTranslationService;
        this.thesaurus = thesaurus;
    }

    RatedPowerSearchableProperty init(SearchDomain domain, SearchablePropertyGroup group, Clock clock) {
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
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_RATEDPOWER.getDisplayName(this.thesaurus);
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof Quantity) {
            return String.valueOf(value).split(" ")[1];
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(FIELD_NAME, PropertyTranslationKeys.USAGEPOINT_RATEDPOWER)
                .fromThesaurus(this.thesaurus)
                .addValues(
                        Quantity.create(BigDecimal.ZERO, 0, "W"),
                        Quantity.create(BigDecimal.ZERO, 3, "W"),
                        Quantity.create(BigDecimal.ZERO, 6, "W"),
                        Quantity.create(BigDecimal.ZERO, 9, "W"),
                        Quantity.create(BigDecimal.ZERO, 12, "W"),
                        Quantity.create(BigDecimal.ZERO, 0, "VA"),
                        Quantity.create(BigDecimal.ZERO, 3, "VA"),
                        Quantity.create(BigDecimal.ZERO, 6, "VA"),
                        Quantity.create(BigDecimal.ZERO, 9, "VA"),
                        Quantity.create(BigDecimal.ZERO, 12, "VA"))
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
        Comparison condition = (Comparison) specification;
        return condition.getOperator()
                .compare(FIELD_NAME, condition.getValues())
                .and(Where.where("detail.interval").isEffective(this.clock.instant()));
    }
}
