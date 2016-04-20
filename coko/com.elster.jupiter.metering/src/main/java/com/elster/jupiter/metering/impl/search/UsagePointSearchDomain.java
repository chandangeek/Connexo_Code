package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;

    // For OSGi purposes
    public UsagePointSearchDomain() {
        super();
    }

    // For Testing purposes
    @Inject
    public UsagePointSearchDomain(PropertySpecService propertySpecService, ServerMeteringService meteringService, NlsService nlsService, Clock clock) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
        this.setNlsService(nlsService);
        this.setClock(clock);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String getId() {
        return UsagePoint.class.getName();
    }

    @Override
    public String displayName() {
        return thesaurus.getFormat(PropertyTranslationKeys.USAGEPOINT_DOMAIN).format();
    }

    @Override
    public Class<?> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public List<String> targetApplications() {
        return Arrays.asList("COKO", "COIN", "COMU");
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return new ArrayList<>(Arrays.asList(
                new MasterResourceIdentifierSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new NameSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new ServiceCategorySearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new ConnectionStateSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new OutageRegionSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new LocationSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus(), this.clock),
                new ConnectionStateSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus())
        ));
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        return this.getProperties();
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
        return new UsagePointFinder(this.toCondition(conditions));
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

    private class UsagePointFinder implements Finder<UsagePoint> {
        private final Finder<UsagePoint> finder;

        private UsagePointFinder(Condition condition) {
            this.finder = DefaultFinder
                    .of(UsagePoint.class, condition, meteringService.getDataModel(), UsagePointMetrologyConfiguration.class)
                    .defaultSortColumn("mRID");
        }

        @Override
        public int count() {
            try (Connection connection = meteringService.getDataModel().getConnection(false)) {
                SqlBuilder countSqlBuilder = new SqlBuilder();
                countSqlBuilder.add(asFragment("count(*)"));
                try (PreparedStatement statement = countSqlBuilder.prepare(connection)) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        resultSet.next();
                        return resultSet.getInt(1);
                    }
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }

        @Override
        public Finder<UsagePoint> paged(int start, int pageSize) {
            return this.finder.paged(start, pageSize);
        }

        @Override
        public Finder<UsagePoint> sorted(String sortColumn, boolean ascending) {
            return this.finder.sorted(sortColumn, ascending);
        }

        @Override
        public List<UsagePoint> find() {
            return this.finder.find();
        }

        @Override
        public Subquery asSubQuery(String... fieldNames) {
            return this.finder.asSubQuery(fieldNames);
        }

        @Override
        public SqlFragment asFragment(String... fieldNames) {
            return this.finder.asFragment(fieldNames);
        }
    }

}