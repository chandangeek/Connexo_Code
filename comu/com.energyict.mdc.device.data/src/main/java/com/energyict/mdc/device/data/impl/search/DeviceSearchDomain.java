package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.elster.jupiter.util.streams.Predicates;
import java.util.HashSet;
import java.util.function.Predicate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Provides an implementation for the {@link SearchDomain} interface
 * that supports {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (15:32)
 */
@Component(name="com.energyict.mdc.device.search", service = SearchDomain.class, immediate = true)
public class DeviceSearchDomain implements SearchDomain {

    private volatile DeviceDataModelService deviceDataModelService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile Clock clock;

    // For OSGi purposes
    public DeviceSearchDomain() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceSearchDomain(DeviceDataModelService deviceDataModelService, Clock clock, ProtocolPluggableService protocolPluggableService) {
        this();
        this.setDeviceDataModelService(deviceDataModelService);
        this.setClock(clock);
        this.setProtocolPluggableService(protocolPluggableService);
    }

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Override
    public String getId() {
        return Device.class.getName();
    }

    @Override
    public boolean supports(Class aClass) {
        return Device.class.equals(aClass);
    }

    @Override
    public List<SearchableProperty> getProperties() {
        List<SearchableProperty> properties = new ArrayList<>();
        properties.addAll(this.fixedProperties());
        properties.addAll(this.connectionTypeProperties());
        return properties;
    }

    private List<SearchableProperty> getProperties(List<ConnectionTypePluggableClass> pluggableClasses) {
        List<SearchableProperty> properties = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>();
        Predicate<SearchableProperty> uniqueName = p -> uniqueNames.add(p.getName());
        this.fixedProperties().stream().filter(uniqueName).forEach(properties::add);
        this.connectionTypeProperties(pluggableClasses).stream().filter(uniqueName).forEach(properties::add);
        return properties;
    }

    private List<SearchableProperty> fixedProperties() {
        DataModel injector = this.deviceDataModelService.dataModel();
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = injector.getInstance(DeviceTypeSearchableProperty.class).init(this);
        return Arrays.asList(
                injector.getInstance(MasterResourceIdentifierSearchableProperty.class).init(this),
                injector.getInstance(SerialNumberSearchableProperty.class).init(this),
                deviceTypeSearchableProperty,
                injector.getInstance(DeviceConfigurationSearchableProperty.class).init(this, deviceTypeSearchableProperty),
                injector.getInstance(StateNameSearchableProperty.class).init(this, deviceTypeSearchableProperty));
    }

    private Collection<? extends SearchableProperty> connectionTypeProperties() {
        return this.connectionTypeProperties(this.protocolPluggableService.findAllConnectionTypePluggableClasses());
    }

    private Collection<? extends SearchableProperty> connectionTypeProperties(List<ConnectionTypePluggableClass> pluggableClasses) {
        return pluggableClasses
                .stream()
                .map(this::toGroup)
                .flatMap(g -> g.getProperties().stream())
                .collect(toList());
    }

    private ConnectionTypeSearchablePropertyGroup toGroup(ConnectionTypePluggableClass pluggableClass) {
        return new ConnectionTypeSearchablePropertyGroup(this, pluggableClass);
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        this.validateConstrictionNames(constrictions);
        this.validateConstrictingValues(constrictions);
        return this.getPropertiesWithConstriction(this.mostUsefulConstriction(constrictions));
    }

    private void validateConstrictionNames(List<SearchablePropertyConstriction> constrictions) {
        Set<String> propertyNames = constrictions
                .stream()
                .map(SearchablePropertyConstriction::getConstrainingProperty)
                .map(SearchableProperty::getName)
                .collect(Collectors.toSet());
        if (!propertyNames.remove(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
            throw new IllegalArgumentException("Constrictions on device type is missing");
        }
        if (!propertyNames.remove(DeviceConfigurationSearchableProperty.PROPERTY_NAME)) {
            throw new IllegalArgumentException("Constrictions on device configuration is missing");
        }
        if (!propertyNames.isEmpty()) {
            throw new IllegalArgumentException("Unexpected constriction for properties: " + propertyNames.stream().collect(Collectors.joining(", ")));
        }
    }

    private void validateConstrictingValues(List<SearchablePropertyConstriction> constrictions) {
        constrictions.forEach(this::validateConstrictingValues);
    }

    private void validateConstrictingValues(SearchablePropertyConstriction constriction) {
        if (constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
            this.validateAllConstrictingValuesAreDeviceTypes(constriction.getConstrainingValues());
        }
        else {
            this.validateAllConstrictingValuesAreDeviceConfigurations(constriction.getConstrainingValues());
        }
    }

    private List<SearchableProperty> getPropertiesWithConstriction(SearchablePropertyConstriction constriction) {
        return this.getProperties(
                    AbstractConnectionTypePluggableClassProvider
                        .from(constriction, this.protocolPluggableService)
                        .getPluggableClasses());
    }

    private void validateAllConstrictingValuesAreDeviceTypes(List<Object> list) {
        Optional<Object> anyNonDeviceType =
                list.stream()
                        .filter(Predicates.not(DeviceType.class::isInstance))
                        .findAny();
        if (anyNonDeviceType.isPresent()) {
            throw new IllegalArgumentException("Constricting values are expected to be of type " + DeviceType.class.getName());
        }
    }

    private void validateAllConstrictingValuesAreDeviceConfigurations(List<Object> list) {
        Optional<Object> anyNonDeviceConfiguration =
                list.stream()
                        .filter(Predicates.not(DeviceConfiguration.class::isInstance))
                        .findAny();
        if (anyNonDeviceConfiguration.isPresent()) {
            throw new IllegalArgumentException("Constricting values are expected to be of configuration " + DeviceConfiguration.class.getName());
        }
    }

    @Override
    public Finder<Device> finderFor(List<SearchablePropertyCondition> conditions) {
        return new DeviceFinder(
                new DeviceSearchSqlBuilder(this.deviceDataModelService.dataModel(), conditions, this.clock.instant()),
                this.deviceDataModelService.dataModel());
    }

    /**
     * Wraps a {@link SearchablePropertyConstriction} to add sorting capabilities.
     */
    private SearchablePropertyConstriction mostUsefulConstriction(List<SearchablePropertyConstriction> constrictions) {
        return constrictions
                .stream()
                .map(AbstractSortableConstriction::from)
                .sorted()
                .map(SortableConstriction::getConstriction)
                .findFirst()
                .get();
    }

    private interface SortableConstriction extends Comparable<SortableConstriction> {
        String getPropertyName();
        SearchablePropertyConstriction getConstriction();

    }
    private abstract static class AbstractSortableConstriction implements SortableConstriction {

        private final SearchablePropertyConstriction constriction;

        private static SortableConstriction from(SearchablePropertyConstriction constriction) {
            if (constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
                return new DeviceTypeConstriction(constriction);
            }
            else {
                return new DeviceConfigurationConstriction(constriction);
            }
        }

        protected AbstractSortableConstriction(SearchablePropertyConstriction deviceTypeConstriction) {
            this.constriction = deviceTypeConstriction;
        }

        @Override
        public String getPropertyName() {
            return this.constriction.getConstrainingProperty().getName();
        }
        @Override
        public SearchablePropertyConstriction getConstriction() {
            return constriction;
        }

    }
    private static class DeviceTypeConstriction extends AbstractSortableConstriction {
        protected DeviceTypeConstriction(SearchablePropertyConstriction deviceTypeConstriction) {
            super(deviceTypeConstriction);
        }

        @Override
        public int compareTo(SortableConstriction that) {
            if (this.getPropertyName().equals(that.getPropertyName())) {
                /* Not expecting to get here as we validate that no constrictions are defined
                 * for the same property but hey, I am a defensive programmer. */
                Integer thisSize = this.getConstriction().getConstrainingValues().size();
                Integer thatSize = that.getConstriction().getConstrainingValues().size();
                return thisSize.compareTo(thatSize);
            }
            else {
                // Must be a DeviceConfigurationConstriction, delegate to it to implement the logic only once
                return that.compareTo(this);
            }
        }
    }

    private static class DeviceConfigurationConstriction extends AbstractSortableConstriction {
        protected DeviceConfigurationConstriction(SearchablePropertyConstriction deviceTypeConstriction) {
            super(deviceTypeConstriction);
        }

        @Override
        public int compareTo(SortableConstriction that) {
            if (this.getPropertyName().equals(that.getPropertyName())) {
                /* Not expecting to get here as we validate that no constrictions are defined
                 * for the same property but hey, I am a defensive programmer. */
                Integer thisSize = this.getConstriction().getConstrainingValues().size();
                Integer thatSize = that.getConstriction().getConstrainingValues().size();
                return thisSize.compareTo(thatSize);
            }
            else {
                /* Must be a DeviceTypeConstriction,
                 * this one always has precedence unless it has no constrictions. */
                if (!this.getConstriction().getConstrainingValues().isEmpty()) {
                    return -1;
                }
                else {
                    return 1;
                }
            }
        }
    }

    /**
     * Provides {@link ConnectionTypePluggableClass} for {@link SearchablePropertyConstriction}s.
     */
    private interface ConnectionTypePluggableClassProvider {
        List<ConnectionTypePluggableClass> getPluggableClasses();
    }

    private static abstract class AbstractConnectionTypePluggableClassProvider implements ConnectionTypePluggableClassProvider {
        static ConnectionTypePluggableClassProvider from(SearchablePropertyConstriction constriction, ProtocolPluggableService protocolPluggableService) {
            if (constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
                return new DeviceTypeConnectionTypePluggableClassProvider(constriction.getConstrainingValues(), protocolPluggableService);
            }
            else {
                return new DeviceConfigurationConnectionTypePluggableClassProvider(constriction.getConstrainingValues());
            }
        }
    }

    private static class DeviceTypeConnectionTypePluggableClassProvider extends AbstractConnectionTypePluggableClassProvider {
        private final ProtocolPluggableService protocolPluggableService;
        private final List<DeviceType> deviceTypes;

        private DeviceTypeConnectionTypePluggableClassProvider(List<Object> deviceTypes, ProtocolPluggableService protocolPluggableService) {
            super();
            this.deviceTypes = deviceTypes.stream().map(DeviceType.class::cast).collect(toList());
            this.protocolPluggableService = protocolPluggableService;
        }

        @Override
        public List<ConnectionTypePluggableClass> getPluggableClasses() {
            return this.deviceTypes
                    .stream()
                    .map(DeviceType::getDeviceProtocolPluggableClass)
                    .map(DeviceProtocolPluggableClass::getDeviceProtocol)
                    .flatMap(p -> p.getSupportedConnectionTypes().stream())
                    .flatMap(ct -> this.protocolPluggableService.findConnectionTypePluggableClassByClassName(ct.getClass().getName()).stream())
                    .collect(toList());
        }
    }

    private static class DeviceConfigurationConnectionTypePluggableClassProvider extends AbstractConnectionTypePluggableClassProvider {
        private final List<DeviceConfiguration> deviceConfigurations;

        private DeviceConfigurationConnectionTypePluggableClassProvider(List<Object> deviceConfigurations) {
            super();
            this.deviceConfigurations = deviceConfigurations.stream().map(DeviceConfiguration.class::cast).collect(toList());
        }

        @Override
        public List<ConnectionTypePluggableClass> getPluggableClasses() {
            return DecoratedStream.decorate(this.deviceConfigurations.stream())
                    .flatMap(config -> config.getPartialConnectionTasks().stream())
                    .map(PartialConnectionTask::getPluggableClass)
                    .distinct(ConnectionTypePluggableClass::getId)
                    .collect(toList());
        }
    }

}