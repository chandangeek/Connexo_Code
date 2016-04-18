package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyValue;

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
    private volatile ServerMetrologyConfigurationService metrologyConfigurationService;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;

    // For OSGi purposes
    public UsagePointSearchDomain() {
        super();
    }

    // For Testing purposes
    @Inject
    public UsagePointSearchDomain(PropertySpecService propertySpecService, ServerMeteringService meteringService, ServerMetrologyConfigurationService metrologyConfigurationService, NlsService nlsService, Clock clock) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
        this.setMetrologyConfigurationService(metrologyConfigurationService);
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
    public final void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = (ServerMetrologyConfigurationService) metrologyConfigurationService;
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
        return this.meteringService.getThesaurus().getFormat(PropertyTranslationKeys.USAGEPOINT_DOMAIN).format();
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
                new ServiceCategorySearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new MasterResourceIdentifierSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new NameSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new ConnectionStateSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new ReadRouteSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new TypeSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new ServicePrioritySearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new MetrologyConfigurationSearchableProperty(this, this.propertySpecService, this.metrologyConfigurationService)
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
        ElectricityAttributesSearchablePropertyGroup electricityGroup = new ElectricityAttributesSearchablePropertyGroup(this.meteringService.getThesaurus());
        if (!constrictions.isEmpty()) {
            constrictions.stream()
                    .filter(constriction -> ServiceCategorySearchableProperty.FIELDNAME.equals(constriction.getConstrainingProperty().getName()))
                    .findAny()
                    .ifPresent(constriction -> {
                        if (constriction.getConstrainingValues().contains(ServiceKind.ELECTRICITY)) {
                            properties.add(new GroundedElectricitySearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new CollarElectricitySearchableProperty(this, this.propertySpecService, this.meteringService
                                    .getThesaurus()));
                            properties.add(new InterruptibleElectricitySearchableProperty(this, this.propertySpecService, this.meteringService
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
        Map<String, SearchablePropertyValue> valuesMap = (constrictions.isEmpty() ? fixedProperties : addDynamicProperties(fixedProperties, constrictions))
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

    private List<SearchableProperty> addDynamicProperties(List<SearchableProperty> fixedProperties, Collection<SearchablePropertyConstriction> constrictions) {
        List<SearchableProperty> properties = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>();
        Predicate<SearchableProperty> uniqueName = p -> uniqueNames.add(p.getName());
        fixedProperties.stream().filter(uniqueName).forEach(properties::add);
        this.getServiceCategoryDynamicProperties(constrictions).stream().filter(uniqueName).forEach(properties::add);
        return properties;
    }

    @Override
    public Finder<?> finderFor(List<SearchablePropertyCondition> conditions) {
        return new UsagePointFinder(this.meteringService, conditions);
    }
}