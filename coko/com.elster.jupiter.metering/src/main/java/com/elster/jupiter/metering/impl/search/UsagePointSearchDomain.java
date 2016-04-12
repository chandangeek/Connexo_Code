package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link SearchDomain} interface
 * that supports {@link UsagePoint}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (14:50)
 */
@Component(name = "com.elster.jupiter.metering.search", service = SearchDomain.class, immediate = true)
public class UsagePointSearchDomain implements SearchDomain {

    private volatile PropertySpecService propertySpecService;
    private volatile ServerMeteringService meteringService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile Thesaurus thesaurus;

    // For OSGi purposes
    public UsagePointSearchDomain() {
        super();
    }

    // For Testing purposes
    @Inject
    public UsagePointSearchDomain(PropertySpecService propertySpecService, ServerMeteringService meteringService, NlsService nlsService) {
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
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
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
    public boolean supports(Class aClass) {
        return UsagePoint.class.equals(aClass);
    }

    @Override
    public List<String> targetApplications() {
        return Arrays.asList("COKO", "COIN", "COMU");
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return new ArrayList<>(Arrays.asList(
                new ServiceCategorySearchableProperty(this, this.propertySpecService, this.meteringService
                        .getThesaurus()),
                new MasterResourceIdentifierSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new NameSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()), new ConnectionStateSearchableProperty(this, this.propertySpecService, this.meteringService
                        .getThesaurus()),
                new ReadRouteSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new TypeSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new ServicePrioritySearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new MetrologyConfigurationSearchableProperty(this, this.propertySpecService, this.metrologyConfigurationService, this.meteringService
                        .getThesaurus())
        ));
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        if (constrictions == null || constrictions.isEmpty()) {
            return getProperties();
        }

        List<SearchableProperty> searchableProperties = new ArrayList<>();
        searchableProperties.addAll(getProperties());
        searchableProperties.addAll(addDynamicProperties(getServiceCategoryDynamicProperties(constrictions), constrictions));

        return searchableProperties;
    }


    private List<SearchableProperty> getServiceCategoryDynamicProperties(Collection<SearchablePropertyConstriction> constrictions) {
        List<SearchableProperty> properties = this.getProperties();
        ElectricityAttributesSearchablePropertyGroup electricityGroup = new ElectricityAttributesSearchablePropertyGroup(this.thesaurus);
        if (!constrictions.isEmpty()) {
            constrictions.stream()
                    .filter(constriction -> ServiceCategorySearchableProperty.FIELDNAME.equals(constriction.getConstrainingProperty()
                            .getName()))
                    .findAny()
                    .ifPresent(constriction -> {
                        if (constriction.getConstrainingValues().contains(ServiceKind.ELECTRICITY)) {
                            properties.add(new GroundedElectricitySearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new CollarElectricitySearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new InterruptibleElectricitySearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new LoadLimitElectricitySearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new EstimatedLoadSearchableProperty(this, this.propertySpecService, electricityGroup, this.meteringService
                                    .getThesaurus()));
                            properties.add(new PhaseCodeSearchableProperty(this, this.propertySpecService, electricityGroup, this.meteringService
                                    .getThesaurus()));
                            properties.add(new LimiterElectricitySearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new RatedPowerSearchableProperty(this, this.propertySpecService, electricityGroup, this.meteringService
                                    .getThesaurus()));
                        }
                        if (constriction.getConstrainingValues().contains(ServiceKind.GAS)) {
                            properties.add(new CollarGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new LimiterGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new LoadLimitGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new LoadLimiterTypeGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new PhysicalCapacityGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new BypassGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new ValveGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new CappedGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new ClampedGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new GroundedGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new InterruptibleGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new PressureGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new BypassStatusGasSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                        }
                        if (constriction.getConstrainingValues().contains(ServiceKind.WATER)) {
                            properties.add(new LimiterWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new LoadLimiterTypeWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new LoadLimitWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new CollarWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new PhysicalCapacityWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new BypassWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new ValveWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new CappedWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new ClampedWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new BypassStatusWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new GroundedWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new PressureWaterSearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                        }

                    });
        }

        return properties;
    }

    @Override
    public List<SearchablePropertyValue> getPropertiesValues(Function<SearchableProperty, SearchablePropertyValue> mapper) {
        // 1) retrieve all fixed search properties
        List<SearchableProperty> fixedProperties = getProperties();
        // 2) check properties which affect available domain properties
        List<SearchablePropertyConstriction> constrictions = fixedProperties.stream()
                .filter(SearchableProperty::affectsAvailableDomainProperties)
                .map(mapper::apply)
                .filter(propertyValue -> propertyValue != null && propertyValue.getValueBean() != null && propertyValue.getValueBean().values != null)
                .map(SearchablePropertyValue::asConstriction)
                .collect(Collectors.toList());
        // 3) update list of available properties and convert these properties into properties values
        Map<String, SearchablePropertyValue> valuesMap = (constrictions.isEmpty() ? getProperties() : addDynamicProperties(fixedProperties, constrictions))
                .stream()
                .map(mapper::apply)
                .filter(propertyValue -> propertyValue != null && propertyValue.getValueBean() != null && propertyValue.getValueBean().values != null)
                .collect(Collectors.toMap(propertyValue -> propertyValue.getProperty().getName(), Function.identity()));
        // 4) refresh all properties with their constrictions
        for (SearchablePropertyValue propertyValue : valuesMap.values()) {
            SearchableProperty property = propertyValue.getProperty();
            property.refreshWithConstrictions(property.getConstraints().stream()
                    .map(constrainingProperty -> valuesMap.get(constrainingProperty.getName()))
                    .filter(Objects::nonNull)
                    .map(SearchablePropertyValue::asConstriction)
                    .collect(Collectors.toList()));
        }
        return new ArrayList<>(valuesMap.values());
    }

    @Override
    public Finder<?> finderFor(List<SearchablePropertyCondition> conditions) {
        return new UsagePointFinder(this.toCondition(conditions));
    }

    private List<SearchableProperty> addDynamicProperties(List<SearchableProperty> fixedProperties, Collection<SearchablePropertyConstriction> constrictions) {
        List<SearchableProperty> properties = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>();
        Predicate<SearchableProperty> uniqueName = p -> uniqueNames.add(p.getName());
        fixedProperties.stream().filter(uniqueName).forEach(properties::add);
        this.getServiceCategoryDynamicProperties(constrictions).stream().filter(uniqueName).forEach(properties::add);
        return properties;
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
                    .of(UsagePoint.class, condition, meteringService.getDataModel(), UsagePointMetrologyConfiguration.class, UsagePointDetail.class)
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