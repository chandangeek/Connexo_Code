package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.UsagePoint;
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

public class UsagePointRequirementSqlBuilder implements Subquery {
    private final SearchDomain searchDomain;
    private final PropertySpecService propertySpecService;
    private final SearchService searchService;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    private UsagePoint usagePoint;
    private UsagePointMetrologyConfigurationImpl metrologyConfiguration;

    @Inject
    public UsagePointRequirementSqlBuilder(UsagePointRequirementsSearchDomain searchDomain, PropertySpecService propertySpecService, SearchService searchService, ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.searchDomain = searchService.findDomain(searchDomain.getId()).get(); // It must exist, we just get the extended domain with CPS support
        this.propertySpecService = propertySpecService;
        this.searchService = searchService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public UsagePointRequirementSqlBuilder init(UsagePoint usagePoint, UsagePointMetrologyConfigurationImpl metrologyConfiguration) {
        this.usagePoint = usagePoint;
        this.metrologyConfiguration = metrologyConfiguration;
        return this;
    }

    @Override
    public SqlFragment toFragment() {
        SearchBuilder<Object> searchBuilder = this.searchService.search(this.searchDomain);
        try {
            for (SearchablePropertyValue value : this.metrologyConfiguration.getUsagePointRequirementSearchableProperties()) {
                value.addAsCondition(searchBuilder);
            }
            addUsagePointIdPropertyCondition(searchBuilder);
        } catch (InvalidValueException e) {
            throw BadUsagePointRequirementException.badValue(metrologyConfigurationService.getThesaurus(), e);
        }
        return searchBuilder.toFinder().asFragment(String.valueOf(this.metrologyConfiguration.getId()));
    }

    private void addUsagePointIdPropertyCondition(SearchBuilder<Object> searchBuilder) throws InvalidValueException {
        UsagePointIdSearchableProperty idProperty = new UsagePointIdSearchableProperty(this.searchDomain, propertySpecService);
        SearchablePropertyValue.ValueBean idBean = new SearchablePropertyValue.ValueBean(idProperty.getName(), SearchablePropertyOperator.EQUAL, Collections.singletonList(String.valueOf(this.usagePoint.getId())));
        new SearchablePropertyValue(idProperty, idBean).addAsCondition(searchBuilder);
    }
}

