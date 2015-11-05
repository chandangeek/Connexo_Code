package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.search.sqlbuilder.DeviceSearchSqlBuilder;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * Provides an implementation for the {@link SearchDomain} interface
 * that supports {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (15:32)
 */
@Component(name = "com.energyict.mdc.device.search", service = SearchDomain.class, immediate = true)
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
        return properties;
    }

    private List<SearchableProperty> getProperties(List<SearchablePropertyConstriction> constrictions) {
        List<SearchableProperty> properties = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>();
        Predicate<SearchableProperty> uniqueName = p -> uniqueNames.add(p.getName());
        this.fixedProperties().stream().filter(uniqueName).forEach(properties::add);
        getProtocolDialectDynamicProperties(constrictions).stream().filter(uniqueName).forEach(properties::add);
        return properties;
    }

    private List<SearchableProperty> getProtocolDialectDynamicProperties(List<SearchablePropertyConstriction> constrictions) {
        Optional<SearchablePropertyConstriction> protocolDialectConstriction = constrictions
                .stream()
                .filter(p -> ProtocolDialectSearchableProperty.PROPERTY_NAME.equals(p.getConstrainingProperty().getName()))
                .findFirst();
        if (protocolDialectConstriction.isPresent()) {
            DataModel injector = this.deviceDataModelService.dataModel();
            ProtocolDialectDynamicSearchableGroup propertiesGroup = injector.getInstance(ProtocolDialectDynamicSearchableGroup.class);
            List<SearchableProperty> dynamicProperties = new ArrayList<>();
            Set<String> uniqueDeviceProtocolDialects = new HashSet<>();
            for (Object value : protocolDialectConstriction.get().getConstrainingValues()) {
                ProtocolDialectSearchableProperty.ProtocolDialect protocolDialect = (ProtocolDialectSearchableProperty.ProtocolDialect) value;
                String deviceProtocolDialectName = protocolDialect.getProtocolDialect().getDeviceProtocolDialectName();
                if (!uniqueDeviceProtocolDialects.add(deviceProtocolDialectName)) {
                    continue;
                }
                String relationTableName = this.protocolPluggableService.getDeviceProtocolDialectUsagePluggableClass(protocolDialect.getPluggableClass(),
                        deviceProtocolDialectName).findRelationType().getDynamicAttributeTableName();
                for (PropertySpec propertySpec : protocolDialect.getProtocolDialect().getPropertySpecs()) {
                    dynamicProperties.add(injector.getInstance(ProtocolDialectDynamicSearchableProperty.class)
                            .init(this, propertiesGroup, propertySpec, protocolDialect, relationTableName));
                }
            }
            return dynamicProperties;
        }
        return Collections.emptyList();
    }

    private List<SearchableProperty> fixedProperties() {
        DataModel injector = this.deviceDataModelService.dataModel();
        DeviceTypeSearchableProperty deviceTypeSearchableProperty = injector.getInstance(DeviceTypeSearchableProperty.class).init(this);
        TopologySearchablePropertyGroup topologyGroup = injector.getInstance(TopologySearchablePropertyGroup.class);
        ValidationSearchablePropertyGroup validationGroup = injector.getInstance(ValidationSearchablePropertyGroup.class);
        EstimationSearchablePropertyGroup estimationGroup = injector.getInstance(EstimationSearchablePropertyGroup.class);
        SecuritySearchablePropertyGroup securityGroup = injector.getInstance(SecuritySearchablePropertyGroup.class);
        RegisterSearchablePropertyGroup registerGroup = injector.getInstance(RegisterSearchablePropertyGroup.class);
        ChannelSearchablePropertyGroup channelGroup = injector.getInstance(ChannelSearchablePropertyGroup.class);
        LogbookSearchablePropertyGroup logbookGroup = injector.getInstance(LogbookSearchablePropertyGroup.class);
        LoadProfileSearchablePropertyGroup loadProfileGroup = injector.getInstance(LoadProfileSearchablePropertyGroup.class);
        ComTaskSearchablePropertyGroup comTaskGroup = injector.getInstance(ComTaskSearchablePropertyGroup.class);
        return Arrays.asList(
                injector.getInstance(MasterResourceIdentifierSearchableProperty.class).init(this),
                injector.getInstance(SerialNumberSearchableProperty.class).init(this),
                deviceTypeSearchableProperty,
                injector.getInstance(DeviceConfigurationSearchableProperty.class).init(this, deviceTypeSearchableProperty),
                injector.getInstance(StateNameSearchableProperty.class).init(this, deviceTypeSearchableProperty),
                injector.getInstance(DeviceGroupSearchableProperty.class).init(this),
                injector.getInstance(BatchSearchableProperty.class).init(this),
                injector.getInstance(YearOfCertificationSearchableProperty.class).init(this),
                injector.getInstance(ConnectionMethodSearchableProperty.class).init(this),
                injector.getInstance(SharedScheduleSearchableProperty.class).init(this),
                injector.getInstance(UsagePointSearchableProperty.class).init(this),
                injector.getInstance(ServiceCategorySearchableProperty.class).init(this),
                injector.getInstance(MasterDeviceSearchableProperty.class).init(this, topologyGroup),
                injector.getInstance(SlaveDeviceSearchableProperty.class).init(this, topologyGroup),
                injector.getInstance(ValidationStatusSearchableProperty.class).init(this, validationGroup),
                injector.getInstance(EstimationStatusSearchableProperty.class).init(this, estimationGroup),
                injector.getInstance(SecurityNameSearchableProperty.class).init(this, deviceTypeSearchableProperty, securityGroup),
                injector.getInstance(RegisterObisCodeSearchableProperty.class).init(this, registerGroup),
                injector.getInstance(RegisterReadingTypeNameSearchableProperty.class).init(this, registerGroup),
                injector.getInstance(RegisterReadingTypeTimeOfUseSearchableProperty.class).init(this, registerGroup),
                injector.getInstance(RegisterReadingTypeUnitOfMeasureSearchableProperty.class).init(this, registerGroup),
                injector.getInstance(ProtocolDialectSearchableProperty.class).init(this, deviceTypeSearchableProperty),
                injector.getInstance(ChannelReadingTypeNameSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(ChannelReadingTypeUnitOfMeasureSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(ChannelReadingTypeTimeOfUseSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(ChannelObisCodeSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(ChannelIntervalSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(LogbookNameSearchableProperty.class).init(this, logbookGroup),
                injector.getInstance(LogbookObisCodeSearchableProperty.class).init(this, logbookGroup),
                injector.getInstance(LoadProfileNameSearchableProperty.class).init(this, loadProfileGroup),
                injector.getInstance(ComTaskNameSearchableProperty.class).init(this, comTaskGroup),
                injector.getInstance(ComTaskSecuritySettingSearchableProperty.class).init(this, comTaskGroup, deviceTypeSearchableProperty),
                injector.getInstance(ComTaskConnectionMethodSearchableProperty.class).init(this, comTaskGroup)
        );
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
        if (constrictions == null || constrictions.isEmpty()) {
            return getProperties();
        }
        return getProperties(constrictions);
    }

    @Override
    public Finder<Device> finderFor(List<SearchablePropertyCondition> conditions) {
        return new DeviceFinder(
                new DeviceSearchSqlBuilder(this.deviceDataModelService.dataModel(), conditions, this.clock.instant()),
                this.deviceDataModelService.dataModel());
    }

    @Override
    public String displayName() {
        return deviceDataModelService.thesaurus().getFormat(PropertyTranslationKeys.DEVICE_DOMAIN).format();
    }


    private interface SortableConstriction extends Comparable<SortableConstriction> {
        String getPropertyName();

        SearchablePropertyConstriction getConstriction();

    }

    /**
     * Provides {@link ConnectionTypePluggableClass} for {@link SearchablePropertyConstriction}s.
     */
    private interface ConnectionTypePluggableClassProvider {
        List<ConnectionTypePluggableClass> getPluggableClasses();
    }

    private abstract static class AbstractSortableConstriction implements SortableConstriction {

        private final SearchablePropertyConstriction constriction;

        protected AbstractSortableConstriction(SearchablePropertyConstriction deviceTypeConstriction) {
            this.constriction = deviceTypeConstriction;
        }

        private static SortableConstriction from(SearchablePropertyConstriction constriction) {
            if (constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
                return new DeviceTypeConstriction(constriction);
            } else {
                return new DeviceConfigurationConstriction(constriction);
            }
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
            } else {
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
            } else {
                /* Must be a DeviceTypeConstriction,
                 * this one always has precedence unless it has no constrictions. */
                if (!this.getConstriction().getConstrainingValues().isEmpty()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    private static abstract class AbstractConnectionTypePluggableClassProvider implements ConnectionTypePluggableClassProvider {
        static ConnectionTypePluggableClassProvider from(SearchablePropertyConstriction constriction, ProtocolPluggableService protocolPluggableService) {
            if (constriction.getConstrainingProperty().hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)) {
                return new DeviceTypeConnectionTypePluggableClassProvider(constriction.getConstrainingValues(), protocolPluggableService);
            } else {
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