/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyContractOnUsagePoint;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MetrologyPurposeSearchableProperty implements SearchableUsagePointProperty {

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final ServerMeteringService meteringService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final Clock clock;
    static final String FIELD_NAME  = "metrologyConfigurations.metrologyConfiguration.metrologyContracts.metrologyPurpose";

    public MetrologyPurposeSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, ServerMetrologyConfigurationService metrologyConfigurationService, ServerMeteringService meteringService, Clock clock) {
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
        this.clock = clock;
    }

    @Override
    public Condition toCondition(Condition specification) {

        return ListOperator.IN.contains(meteringService.getDataModel()
                        .query(EffectiveMetrologyConfigurationOnUsagePoint.class, EffectiveMetrologyContractOnUsagePoint.class)
                        .asSubquery(ListOperator.IN.contains(meteringService.getDataModel()
                                        .query(EffectiveMetrologyContractOnUsagePoint.class, MetrologyContract.class, MetrologyPurpose.class)
                                        .asSubquery(Where.where("interval")
                                                        .isEffective()
                                                        .and(Where.where("metrologyContract.metrologyPurpose")
                                                                .in(new ArrayList<>(((Contains) specification).getCollection())))
                                                , "metrologyConfiguration"),
                                "id"), "usagePoint"),
                "id");
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
    public PropertySpec getSpecification() {
        metrologyConfigurationService.getMetrologyPurposes();
        return this.propertySpecService
                .referenceSpec(MetrologyPurpose.class)
                .named(FIELD_NAME, PropertyTranslationKeys.USAGEPOINT_PURPOSE)
                .fromThesaurus(this.metrologyConfigurationService.getThesaurus())
                .addValues(metrologyConfigurationService.getMetrologyPurposes())
                .markExhaustive()
                .finish();
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
        return PropertyTranslationKeys.USAGEPOINT_PURPOSE.getDisplayName(metrologyConfigurationService.getThesaurus());
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof MetrologyPurpose) {
            return ((MetrologyPurpose) value).getName();
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
}
