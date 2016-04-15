package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
                        .getThesaurus()),
                new OutageRegionSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new LocationSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus(), this.clock),
                new ConnectionStateSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus())
        ));
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        List<SearchableProperty> searchableProperties = getProperties();
        if (constrictions != null && !constrictions.isEmpty()) {
            searchableProperties.addAll(getServiceCategoryDynamicProperties(constrictions));
        }
        return searchableProperties;
    }

    private List<SearchableProperty> getServiceCategoryDynamicProperties(Collection<SearchablePropertyConstriction> constrictions) {
        List<SearchableProperty> properties = new ArrayList<>();
        DataModel injector = this.meteringService.getDataModel();
        ElectricityAttributesSearchablePropertyGroup electricityGroup = injector.getInstance(ElectricityAttributesSearchablePropertyGroup.class);
        GasAttributesSearchablePropertyGroup gasGroup = injector.getInstance(GasAttributesSearchablePropertyGroup.class);
        WaterAttributesSearchablePropertyGroup waterGroup = injector.getInstance(WaterAttributesSearchablePropertyGroup.class);
        ThermalAttributesSearchablePropertyGroup thermalGroup = injector.getInstance(ThermalAttributesSearchablePropertyGroup.class);
        constrictions.stream()
                .filter(constriction -> ServiceCategorySearchableProperty.FIELD_NAME
                        .equals(constriction.getConstrainingProperty().getName()))
                .findAny()
                .ifPresent(constriction -> constriction.getConstrainingValues().forEach(value -> {
                    if (value instanceof ServiceKind) {
                        switch ((ServiceKind) value) {
                            case ELECTRICITY:
                                properties.add(injector.getInstance(GroundedSearchableProperty.class)
                                        .init(this, electricityGroup));
                                properties.add(injector.getInstance(InterruptibleSearchableProperty.class)
                                        .init(this, electricityGroup));
                                properties.add(injector.getInstance(LoadLimitSearchableProperty.class)
                                        .init(this, electricityGroup));
                                properties.add(injector.getInstance(LoadLimiterTypeSearchableProperty.class)
                                        .init(this, electricityGroup));
                                properties.add(injector.getInstance(EstimatedLoadSearchableProperty.class)
                                        .init(this, electricityGroup));
                                properties.add(injector.getInstance(PhaseCodeSearchableProperty.class)
                                        .init(this, electricityGroup));
                                properties.add(injector.getInstance(LimiterSearchableProperty.class)
                                        .init(this, electricityGroup));
                                properties.add(injector.getInstance(RatedPowerSearchableProperty.class)
                                        .init(this, electricityGroup));
                                properties.add(injector.getInstance(RatedCurrentSearchableProperty.class)
                                        .init(this, electricityGroup));
                                break;
                            case GAS:
                                properties.add(injector.getInstance(CollarSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(LimiterSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(LoadLimitSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(LoadLimiterTypeSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(PhysicalCapacitySearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(BypassSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(ValveSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(CappedSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(ClampedSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(GroundedSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(InterruptibleSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(PressureSearchableProperty.class)
                                        .init(this, gasGroup));
                                properties.add(injector.getInstance(BypassStatusSearchableProperty.class)
                                        .init(this, gasGroup));
                                break;
                            case WATER:
                                properties.add(injector.getInstance(CollarSearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(LimiterSearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(LoadLimiterTypeSearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(LoadLimitSearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(PhysicalCapacitySearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(BypassSearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(ValveSearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(CappedSearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(ClampedSearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(BypassStatusSearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(GroundedSearchableProperty.class)
                                        .init(this, waterGroup));
                                properties.add(injector.getInstance(PressureSearchableProperty.class)
                                        .init(this, waterGroup));
                                break;
                            case HEAT:
                                properties.add(injector.getInstance(CollarSearchableProperty.class)
                                        .init(this, thermalGroup));
                                properties.add(injector.getInstance(PhysicalCapacitySearchableProperty.class)
                                        .init(this, thermalGroup));
                                properties.add(injector.getInstance(BypassSearchableProperty.class)
                                        .init(this, thermalGroup));
                                properties.add(injector.getInstance(ValveSearchableProperty.class)
                                        .init(this, thermalGroup));
                                properties.add(injector.getInstance(BypassStatusSearchableProperty.class)
                                        .init(this, thermalGroup));
                                properties.add(injector.getInstance(PressureSearchableProperty.class)
                                        .init(this, thermalGroup));
                        }
                    } else {
                        throw new IllegalArgumentException("Value is not compatible with the property");
                    }
                }));
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
        Map<String, SearchablePropertyValue> valuesMap = getPropertiesWithConstrictions(constrictions)
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
        return new UsagePointFinder(toCondition(conditions));
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