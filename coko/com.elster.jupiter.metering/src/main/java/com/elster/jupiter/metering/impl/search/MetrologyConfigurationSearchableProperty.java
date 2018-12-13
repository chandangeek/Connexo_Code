/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MetrologyConfigurationSearchableProperty implements SearchableUsagePointProperty {

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private Clock clock;
    static final String FIELD_NAME = "metrologyConfigurations.metrologyConfiguration";

    public MetrologyConfigurationSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, ServerMetrologyConfigurationService metrologyConfigurationService, Clock clock) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.clock = clock;
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
    public PropertySpec getSpecification() {
        MetrologyConfiguration[] metrologyConfigurations =
                metrologyConfigurationService.findAllMetrologyConfigurations().stream().toArray(MetrologyConfiguration[]::new);
        return this.propertySpecService
                .referenceSpec(MetrologyConfiguration.class)
                .named(FIELD_NAME, PropertyTranslationKeys.USAGEPOINT_METROLOGYCONFIGURATION)
                .fromThesaurus(this.metrologyConfigurationService.getThesaurus())
                .addValues(metrologyConfigurations)
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
        return PropertyTranslationKeys.USAGEPOINT_METROLOGYCONFIGURATION.getDisplayName(this.metrologyConfigurationService.getThesaurus());
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof MetrologyConfiguration) {
            return ((MetrologyConfiguration) value).getName();
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
        return specification.and(Where.where("metrologyConfigurations.interval")
                .isEffective(this.clock.instant()));
    }
}
