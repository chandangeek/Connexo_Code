package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.conditions.Condition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link SearchDomain} interface
 * that supports {@link UsagePoint}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (14:50)
 */
@Component(name="com.elster.jupiter.metering.search", service = SearchDomain.class, immediate = true)
public class UsagePointSearchDomain implements SearchDomain {

    private volatile PropertySpecService propertySpecService;
    private volatile ServerMeteringService meteringService;

    // For OSGi purposes
    public UsagePointSearchDomain() {
        super();
    }

    // For Testing purposes
    @Inject
    public UsagePointSearchDomain(PropertySpecService propertySpecService, ServerMeteringService meteringService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public String getId() {
        return UsagePoint.class.getName();
    }

    @Override
    public boolean supports(Class aClass) {
        return UsagePoint.class.equals(aClass);
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return Collections.singletonList(
                new MasterResourceIdentifierSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()));
    }

    @Override
    public Finder<?> finderFor(List<SearchablePropertyCondition> conditions) {
        return DefaultFinder.of(UsagePoint.class, this.toCondition(conditions), this.meteringService.getDataModel());
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