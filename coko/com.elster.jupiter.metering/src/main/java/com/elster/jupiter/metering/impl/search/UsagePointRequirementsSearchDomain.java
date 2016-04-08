package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UsagePointRequirementsSearchDomain implements SearchDomain {
    private PropertySpecService propertySpecService;
    private ServerMeteringService meteringService;

    @SuppressWarnings("unused") // OSGI
    public UsagePointRequirementsSearchDomain() {
    }

    @Inject
    public UsagePointRequirementsSearchDomain(PropertySpecService propertySpecService, ServerMeteringService meteringService) {
        this.propertySpecService = propertySpecService;
        this.meteringService = meteringService;
    }

    @Override
    public List<String> targetApplications() {
        return Collections.singletonList("NONE");
    }

    @Override
    public String getId() {
        return UsagePoint.class.getName() + "-Requirements";
    }

    @Override
    public String displayName() {
        return this.meteringService.getThesaurus().getFormat(PropertyTranslationKeys.USAGE_POINT_REQUIREMENT_SEARCH_DOMAIN).format();
    }

    @Override
    public boolean supports(Class domainClass) {
        return UsagePoint.class.isAssignableFrom(domainClass);
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return Arrays.asList(
                new ServiceCategorySearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new OutageRegionSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus())
        );
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        return getProperties();
    }

    @Override
    public List<SearchablePropertyValue> getPropertiesValues(Function<SearchableProperty, SearchablePropertyValue> mapper) {
        return getProperties()
                .stream()
                .map(mapper::apply)
                .filter(propertyValue -> propertyValue != null && propertyValue.getValueBean() != null && propertyValue.getValueBean().values != null)
                .collect(Collectors.toList());
    }

    @Override
    public Finder<?> finderFor(List<SearchablePropertyCondition> conditions) {
        return new UsagePointFinder(this.meteringService, this.toCondition(conditions));
    }

    private Condition toCondition(List<SearchablePropertyCondition> conditions) {
        return conditions
                .stream()
                .map(ConditionBuilder::new)
                .reduce(
                        Condition.TRUE,
                        (underConstruction, builder) -> underConstruction.and(builder.build()),
                        Condition::and);
    }

    private class ConditionBuilder {
        private final SearchablePropertyCondition spec;
        private final SearchableUsagePointProperty property;

        private ConditionBuilder(SearchablePropertyCondition spec) {
            super();
            this.spec = spec;
            this.property = (SearchableUsagePointProperty) spec.getProperty();
        }

        private Condition build() {
            return this.property.toCondition(this.spec.getCondition());
        }
    }
}
