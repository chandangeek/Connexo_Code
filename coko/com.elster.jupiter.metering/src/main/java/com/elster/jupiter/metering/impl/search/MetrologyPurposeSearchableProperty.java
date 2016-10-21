package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
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

public class MetrologyPurposeSearchableProperty implements SearchableUsagePointProperty {

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    static final String FIELD_NAME  = "metrologyConfigurations.metrologyConfiguration.metrologyContracts.metrologyPurpose";

    public MetrologyPurposeSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, ServerMetrologyConfigurationService metrologyConfigurationService){
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public Condition toCondition(Condition specification) {
        return specification;
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
