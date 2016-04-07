package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointRequirement;
import com.elster.jupiter.metering.impl.search.UsagePointIdSearchableProperty;
import com.elster.jupiter.metering.impl.search.UsagePointRequirementsSearchDomain;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsagePointRequirementSqlBuilder implements Subquery {
    private final SearchDomain searchDomain;
    private final PropertySpecService propertySpecService;
    private final SearchService searchService;

    private UsagePoint usagePoint;
    private UsagePointMetrologyConfiguration metrologyConfiguration;

    @Inject
    public UsagePointRequirementSqlBuilder(UsagePointRequirementsSearchDomain searchDomain, PropertySpecService propertySpecService, SearchService searchService) {
        this.searchDomain = searchDomain;
        this.propertySpecService = propertySpecService;
        this.searchService = searchService;
    }

    public UsagePointRequirementSqlBuilder init(UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration) {
        this.usagePoint = usagePoint;
        this.metrologyConfiguration = metrologyConfiguration;
        return this;
    }

    @Override
    public SqlFragment toFragment() {
        SearchBuilder<Object> searchBuilder = this.searchService.search(this.searchDomain);
        Map<String, SearchablePropertyValue.ValueBean> searchableProperties = this.metrologyConfiguration.getUsagePointRequirements()
                .stream()
                .collect(Collectors.toMap(UsagePointRequirement::getSearchablePropertyName, UsagePointRequirement::toValueBean));
        try {
            for (SearchablePropertyValue value : getConvertedPropertiesValues(searchableProperties)) {
                value.addAsCondition(searchBuilder);
            }
            addUsagePointIdPropertyCondition(searchBuilder);
        } catch (InvalidValueException e) {
            // TODO throw some exception
        }
        return searchBuilder.toFinder().asFragment(String.valueOf(this.metrologyConfiguration.getId()));
    }

    private List<SearchablePropertyValue> getConvertedPropertiesValues(Map<String, SearchablePropertyValue.ValueBean> searchableProperties) {
        return this.searchDomain
                .getPropertiesValues(property -> new SearchablePropertyValue(property, searchableProperties.get(property.getName())));
    }

    private void addUsagePointIdPropertyCondition(SearchBuilder<Object> searchBuilder) throws InvalidValueException {
        UsagePointIdSearchableProperty idProperty = new UsagePointIdSearchableProperty(this.searchDomain, propertySpecService);
        SearchablePropertyValue.ValueBean idBean = new SearchablePropertyValue.ValueBean();
        idBean.propertyName = idProperty.getName();
        idBean.operator = SearchablePropertyOperator.EQUAL;
        idBean.values = Collections.singletonList(String.valueOf(this.usagePoint.getId()));
        new SearchablePropertyValue(idProperty, idBean).addAsCondition(searchBuilder);
    }
}

