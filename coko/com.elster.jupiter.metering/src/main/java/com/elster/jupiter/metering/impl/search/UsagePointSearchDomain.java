package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
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
    private volatile MeteringTranslationService meteringTranslationService;
    private volatile ServerMetrologyConfigurationService metrologyConfigurationService;
    private volatile Clock clock;
    private volatile LicenseService licenseService;
    private volatile UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    // For OSGi purposes
    public UsagePointSearchDomain() {
        super();
    }

    // For Testing purposes
    @Inject
    public UsagePointSearchDomain(PropertySpecService propertySpecService,
                                  ServerMeteringService meteringService,
                                  MeteringTranslationService meteringTranslationService,
                                  ServerMetrologyConfigurationService metrologyConfigurationService,
                                  Clock clock,
                                  LicenseService licenseService,
                                  UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
        this.setMeteringTranslationService(meteringTranslationService);
        this.setServerMetrologyConfigurationService(metrologyConfigurationService);
        this.setClock(clock);
        this.setLicenseService(licenseService);
        this.setUsagePointLifeCycleConfigurationService(usagePointLifeCycleConfigurationService);
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
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setServerMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = (ServerMetrologyConfigurationService) metrologyConfigurationService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    public void setUsagePointLifeCycleConfigurationService(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    @Override
    public String getId() {
        return UsagePoint.class.getName();
    }

    @Override
    public String displayName() {
        return this.meteringService.getThesaurus().getFormat(PropertyTranslationKeys.USAGEPOINT_DOMAIN).format();
    }

    @Override
    public Class<?> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public List<String> targetApplications() {
        if (this.licenseService.getLicenseForApplication("INS").isPresent()) {
            return Arrays.asList("COKO", "COIN");
        }
        return Arrays.asList("COKO", "COIN", "COMU");
    }

    @Override
    public List<SearchableProperty> getProperties() {
        if (this.isMultisensePropertiesOnly()) {
            return this.getMultisenseProperties();
        }

        return this.getInsightProperties();
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        List<SearchableProperty> searchableProperties = getProperties();
        if (constrictions != null && !constrictions.isEmpty()) {
            if (this.isMultisensePropertiesOnly()) {
                return searchableProperties;
            }
            searchableProperties.addAll(getServiceCategoryDynamicProperties(constrictions));
        }
        return searchableProperties;
    }

    /*
     * Be aware that subclasses can filter out some properties
     */
    protected List<SearchableProperty> getServiceCategoryDynamicProperties(Collection<SearchablePropertyConstriction> constrictions) {
        List<SearchableProperty> properties = new ArrayList<>();
        DataModel injector = this.metrologyConfigurationService.getDataModel();
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
                                properties.add(injector.getInstance(CollarSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                properties.add(injector.getInstance(GroundedSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                properties.add(injector.getInstance(InterruptibleSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                properties.add(injector.getInstance(LoadLimitSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                properties.add(injector.getInstance(LoadLimiterTypeSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                properties.add(injector.getInstance(EstimatedLoadSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                properties.add(injector.getInstance(NominalServiceVoltageSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                properties.add(injector.getInstance(PhaseCodeSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                properties.add(injector.getInstance(LimiterSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                properties.add(injector.getInstance(RatedPowerSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                properties.add(injector.getInstance(RatedCurrentSearchableProperty.class)
                                        .init(this, electricityGroup, this.clock));
                                break;
                            case GAS:
                                properties.add(injector.getInstance(CollarSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(LimiterSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(GasLoadLimitSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(LoadLimiterTypeSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(PhysicalCapacitySearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(BypassSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(ValveSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(CappedSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(ClampedSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(GroundedSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(InterruptibleSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(PressureSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                properties.add(injector.getInstance(BypassStatusSearchableProperty.class)
                                        .init(this, gasGroup, this.clock));
                                break;
                            case WATER:
                                properties.add(injector.getInstance(CollarSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(LimiterSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(LoadLimiterTypeSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(WaterLoadLimitSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(PhysicalCapacitySearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(BypassSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(ValveSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(CappedSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(ClampedSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(BypassStatusSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(GroundedSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                properties.add(injector.getInstance(PressureSearchableProperty.class)
                                        .init(this, waterGroup, this.clock));
                                break;
                            case HEAT:
                                properties.add(injector.getInstance(CollarSearchableProperty.class)
                                        .init(this, thermalGroup, this.clock));
                                properties.add(injector.getInstance(ThermalPhysicalCapacitySearchableProperty.class)
                                        .init(this, thermalGroup, this.clock));
                                properties.add(injector.getInstance(BypassSearchableProperty.class)
                                        .init(this, thermalGroup, this.clock));
                                properties.add(injector.getInstance(ValveSearchableProperty.class)
                                        .init(this, thermalGroup, this.clock));
                                properties.add(injector.getInstance(BypassStatusSearchableProperty.class)
                                        .init(this, thermalGroup, this.clock));
                                properties.add(injector.getInstance(PressureSearchableProperty.class)
                                        .init(this, thermalGroup, this.clock));
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
                .map(mapper)
                .filter(propertyValue -> propertyValue != null && propertyValue.getValueBean() != null && propertyValue
                        .getValueBean().values != null)
                .map(SearchablePropertyValue::asConstriction)
                .collect(Collectors.toList());
        // 3) update list of available properties and convert these properties into properties values
        Map<String, SearchablePropertyValue> valuesMap = (constrictions.isEmpty() ? fixedProperties : addDynamicProperties(fixedProperties, constrictions))
                .stream()
                .map(mapper)
                .filter(propertyValue -> propertyValue != null && propertyValue.getValueBean() != null && propertyValue
                        .getValueBean().values != null)
                .collect(Collectors.toMap(propertyValue -> propertyValue.getProperty()
                        .getName(), Function.identity()));
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

    private List<SearchableProperty> addDynamicProperties(List<SearchableProperty> fixedProperties, Collection<SearchablePropertyConstriction> constrictions) {
        List<SearchableProperty> properties = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>();
        Predicate<SearchableProperty> uniqueName = p -> uniqueNames.add(p.getName());
        fixedProperties.stream().filter(uniqueName).forEach(properties::add);
        this.getServiceCategoryDynamicProperties(constrictions)
                .stream()
                .filter(uniqueName)
                .forEach(properties::add);
        return properties;
    }

    private List<SearchableProperty> getMultisenseProperties() {
        return Arrays.asList(new NameSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new ServiceCategorySearchableProperty(this, this.propertySpecService, meteringTranslationService, this.meteringService.getThesaurus()),
                new MetrologyConfigurationSearchableProperty(this, this.propertySpecService, this.metrologyConfigurationService, this.clock),
                new InstallationTimeSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new MasterResourceIdentifierSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()));
    }

    private List<SearchableProperty> getInsightProperties() {
        return new ArrayList<>(Arrays.asList(
                new NameSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new ServiceCategorySearchableProperty(this, this.propertySpecService, meteringTranslationService, this.meteringService.getThesaurus()),
                new MetrologyConfigurationSearchableProperty(this, this.propertySpecService, this.metrologyConfigurationService, this.clock),
                new MetrologyPurposeSearchableProperty(this, this.propertySpecService, this.metrologyConfigurationService, this.clock),
                new UsagePointStateSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus(), this.usagePointLifeCycleConfigurationService),
                new ConnectionStateSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus(), this.clock),
                new LocationSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus(), this.clock),
                new InstallationTimeSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new MasterResourceIdentifierSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new TypeSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new ReadRouteSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new ServicePrioritySearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new MeterSearchableProperty(this, this.propertySpecService, this.meteringService)));
    }

    protected boolean isMultisensePropertiesOnly() {
        return !this.licenseService.getLicenseForApplication("INS")
                .isPresent() && this.licenseService.getLicenseForApplication("MDC").isPresent();
    }

    @Override
    public Finder<?> finderFor(List<SearchablePropertyCondition> conditions) {
        return new UsagePointFinder(this.meteringService, conditions);
    }

    protected ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return this.metrologyConfigurationService;
    }

    protected PropertySpecService getPropertySpecService() {
        return this.propertySpecService;
    }

    protected MeteringTranslationService getMeteringTranslationService() {
        return meteringTranslationService;
    }
}
