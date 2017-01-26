package com.elster.jupiter.metering.impl.search.enddevice;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.ServerMeteringService;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link SearchDomain} interface
 * that supports {@link EndDevice}s.
 */
@Component(name = "com.elster.jupiter.metering.search.enddevice", service = SearchDomain.class, immediate = true)
public class EndDeviceSearchDomain implements SearchDomain {

    private volatile PropertySpecService propertySpecService;
    private volatile ServerMeteringService meteringService;
    private volatile Thesaurus thesaurus;

    // For OSGi purposes
    public EndDeviceSearchDomain() {
        super();
    }

    // For Testing purposes
    @Inject
    public EndDeviceSearchDomain(PropertySpecService propertySpecService, ServerMeteringService meteringService, NlsService nlsService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
        this.setNlsService(nlsService);
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
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public String getId() {
        return EndDevice.class.getName();
    }

    @Override
    public Class<?> getDomainClass() {
        return EndDevice.class;
    }

    @Override
    public List<String> targetApplications() {
        return Arrays.asList("COKO", "COIN");
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return new ArrayList<>(Arrays.asList(
                new NameSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new MasterResourceIdentifierSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus())));
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        if (!constrictions.isEmpty()) {
            throw new IllegalArgumentException("Expecting no constrictions");
        } else {
            return this.getProperties();
        }
    }

    @Override
    public List<SearchablePropertyValue> getPropertiesValues(Function<SearchableProperty, SearchablePropertyValue> mapper) {
        return getProperties()
                .stream()
                .map(mapper)
                .filter(propertyValue -> propertyValue != null && propertyValue.getValueBean() != null && propertyValue.getValueBean().isValid())
                .collect(Collectors.toList());
    }

    @Override
    public Finder<EndDevice> finderFor(List<SearchablePropertyCondition> conditions) {
        return new EndDeviceFinder(this.toCondition(conditions));
    }

    @Override
    public String displayName() {
        return thesaurus.getFormat(PropertyTranslationKeys.ENDDEVICE_DOMAIN).format();
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

    private final class ConditionBuilder {
        private final SearchablePropertyCondition spec;
        private final SearchableEndDeviceProperty property;

        private ConditionBuilder(SearchablePropertyCondition spec) {
            super();
            this.spec = spec;
            this.property = (SearchableEndDeviceProperty) spec.getProperty();
        }

        private Condition build() {
            return this.property.toCondition(this.spec.getCondition());
        }

    }

    private final class EndDeviceFinder implements Finder<EndDevice> {
        private final Finder<EndDevice> finder;

        private EndDeviceFinder(Condition condition) {
            this.finder = DefaultFinder
                    .of(EndDevice.class, condition, meteringService.getDataModel())
                    .defaultSortColumn("name");
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
        public Finder<EndDevice> paged(int start, int pageSize) {
            return this.finder.paged(start, pageSize);
        }

        @Override
        public Finder<EndDevice> sorted(String sortColumn, boolean ascending) {
            return this.finder.sorted(sortColumn, ascending);
        }

        @Override
        public List<EndDevice> find() {
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
