/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.energyict.mdc.common.search.SearchDomains;
import com.energyict.mdc.device.config.DeviceType;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    public String displayName() {
        return deviceDataModelService.thesaurus().getFormat(PropertyTranslationKeys.DEVICE_DOMAIN).format();
    }

    @Override
    public List<String> targetApplications() {
        return Collections.singletonList(SearchDomains.SEARCH_DOMAIN_APPLICATION_KEY);
    }

    @Override
    public Class<?> getDomainClass() {
        return Device.class;
    }

    @Override
    public List<SearchableProperty> getProperties() {
        DataModel injector = this.deviceDataModelService.dataModel();
        DeviceAttributesPropertyGroup deviceAttributesPropertyGroup = injector.getInstance(DeviceAttributesPropertyGroup.class);
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
        ConnectionSearchablePropertyGroup connectionGroup = injector.getInstance(ConnectionSearchablePropertyGroup.class);
        TransitionSearchablePropertyGroup transitionGroup = injector.getInstance(TransitionSearchablePropertyGroup.class);
        return Arrays.asList(
                injector.getInstance(NameSearchableProperty.class).init(this),
                injector.getInstance(MridSearchableProperty.class).init(this),
                injector.getInstance(SerialNumberSearchableProperty.class).init(this),
                deviceTypeSearchableProperty,
                injector.getInstance(DeviceConfigurationSearchableProperty.class).init(this, deviceTypeSearchableProperty),
                injector.getInstance(StateNameSearchableProperty.class).init(this, deviceTypeSearchableProperty),
                injector.getInstance(DeviceGroupSearchableProperty.class).init(this),
                injector.getInstance(BatchSearchableProperty.class).init(this, deviceAttributesPropertyGroup),
                injector.getInstance(YearOfCertificationSearchableProperty.class).init(this, deviceAttributesPropertyGroup),
                injector.getInstance(ManufacturerSearchableProperty.class).init(this, deviceAttributesPropertyGroup),
                injector.getInstance(ModelNumberSearchableProperty.class).init(this, deviceAttributesPropertyGroup),
                injector.getInstance(ModelVersionSearchableProperty.class).init(this, deviceAttributesPropertyGroup),
                injector.getInstance(ConnectionMethodSearchableProperty.class).init(this),
                injector.getInstance(SharedScheduleSearchableProperty.class).init(this),
                injector.getInstance(UsagePointSearchableProperty.class).init(this),
                injector.getInstance(MasterDeviceSearchableProperty.class).init(this, topologyGroup),
                injector.getInstance(SlaveDeviceSearchableProperty.class).init(this, topologyGroup),
                injector.getInstance(ValidationStatusSearchableProperty.class).init(this, validationGroup),
                injector.getInstance(EstimationStatusSearchableProperty.class).init(this, estimationGroup),
                injector.getInstance(SecurityNameSearchableProperty.class).init(this, deviceTypeSearchableProperty, securityGroup),
                injector.getInstance(RegisterObisCodeSearchableProperty.class).init(this, registerGroup),
                injector.getInstance(RegisterReadingTypeNameSearchableProperty.class).init(this, registerGroup),
                injector.getInstance(RegisterReadingTypeTimeOfUseSearchableProperty.class).init(this, registerGroup),
                injector.getInstance(RegisterReadingTypeUnitOfMeasureSearchableProperty.class).init(this, registerGroup),
                injector.getInstance(RegisterLastReadingSearchableProperty.class).init(this, registerGroup),
                injector.getInstance(ProtocolDialectSearchableProperty.class).init(this, deviceTypeSearchableProperty),
                injector.getInstance(ChannelReadingTypeNameSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(ChannelReadingTypeUnitOfMeasureSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(ChannelReadingTypeTimeOfUseSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(ChannelObisCodeSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(ChannelIntervalSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(ChannelLastReadingSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(ChannelLastValueSearchableProperty.class).init(this, channelGroup),
                injector.getInstance(LogbookNameSearchableProperty.class).init(this, logbookGroup),
                injector.getInstance(LogbookObisCodeSearchableProperty.class).init(this, logbookGroup),
                injector.getInstance(LogbookLastReadingSearchableProperty.class).init(this, logbookGroup),
                injector.getInstance(LogbookLastEventTimestampSearchableProperty.class).init(this, logbookGroup),
                injector.getInstance(LoadProfileNameSearchableProperty.class).init(this, loadProfileGroup),
                injector.getInstance(LoadProfileLastReadingSearchableProperty.class).init(this, loadProfileGroup),
                injector.getInstance(ComTaskNameSearchableProperty.class).init(this, comTaskGroup),
                injector.getInstance(ComTaskSecuritySettingSearchableProperty.class).init(this, comTaskGroup, deviceTypeSearchableProperty),
                injector.getInstance(ComTaskConnectionMethodSearchableProperty.class).init(this, comTaskGroup),
                injector.getInstance(ComTaskUrgencySearchableProperty.class).init(this, comTaskGroup),
                injector.getInstance(ComTaskNextCommunicationSearchableProperty.class).init(this, comTaskGroup),
                injector.getInstance(ComTaskLastCommunicationSearchableProperty.class).init(this, comTaskGroup),
                injector.getInstance(ComTaskStatusSearchableProperty.class).init(this, comTaskGroup),
                injector.getInstance(ComTaskScheduleTypeSearchableProperty.class).init(this, comTaskGroup),
                injector.getInstance(ComTaskScheduleNameSearchableProperty.class).init(this, comTaskGroup),
                injector.getInstance(ComTaskPlannedDateSearchableProperty.class).init(this, comTaskGroup),
                injector.getInstance(ConnectionNameSearchableProperty.class).init(this, connectionGroup),
                injector.getInstance(ConnectionDirectionSearchableProperty.class).init(this, connectionGroup),
                injector.getInstance(ConnectionCommunicationPortPoolSearchableProperty.class).init(this, connectionGroup),
                injector.getInstance(ConnectionSimultaneousSearchableProperty.class).init(this, connectionGroup),
                injector.getInstance(ConnectionStatusSearchableProperty.class).init(this, connectionGroup),
                injector.getInstance(TransitionShipmentDateSearchableProperty.class).init(this, transitionGroup),
                injector.getInstance(TransitionInstallationDateSearchableProperty.class).init(this, transitionGroup),
                injector.getInstance(TransitionDeactivationDateSearchableProperty.class).init(this, transitionGroup),
                injector.getInstance(TransitionDecommissioningDateSearchableProperty.class).init(this, transitionGroup),
                injector.getInstance(LocationSearchableProperty.class).init(this)
        );
    }

    private List<SearchableProperty> addDynamicProperties(List<SearchableProperty> fixedProperties, Collection<SearchablePropertyConstriction> constrictions) {
        List<SearchableProperty> properties = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>();
        Predicate<SearchableProperty> uniqueName = p -> uniqueNames.add(p.getName());
        fixedProperties.stream().filter(uniqueName).forEach(properties::add);
        getProtocolDialectDynamicProperties(constrictions).stream().filter(uniqueName).forEach(properties::add);
        getGeneralAttributesDynamicProperties(constrictions).stream().filter(uniqueName).forEach(properties::add);
        getConnectionDynamicProperties(constrictions).stream().filter(uniqueName).forEach(properties::add);
        return properties;
    }

    private List<SearchableProperty> getProtocolDialectDynamicProperties(Collection<SearchablePropertyConstriction> constrictions) {
        Optional<SearchablePropertyConstriction> protocolDialectConstriction = constrictions
                .stream()
                .filter(p -> ProtocolDialectSearchableProperty.PROPERTY_NAME.equals(p.getConstrainingProperty().getName()))
                .findFirst();
        if (protocolDialectConstriction.isPresent()) {
            DataModel injector = this.deviceDataModelService.dataModel();
            SearchablePropertyGroup propertiesGroup = injector.getInstance(ProtocolDialectDynamicSearchableGroup.class);
            List<SearchableProperty> dynamicProperties = new ArrayList<>();
            Set<String> uniqueDeviceProtocolDialects = new HashSet<>();
            for (Object value : protocolDialectConstriction.get().getConstrainingValues()) {
                ProtocolDialectSearchableProperty.ProtocolDialect protocolDialect = (ProtocolDialectSearchableProperty.ProtocolDialect) value;
                String deviceProtocolDialectName = protocolDialect.getProtocolDialect().getDeviceProtocolDialectName();
                if (!uniqueDeviceProtocolDialects.add(deviceProtocolDialectName)) {
                    continue;
                }
                this.protocolPluggableService
                        .getDeviceProtocolDialectUsagePluggableClass(protocolDialect.getPluggableClass(), deviceProtocolDialectName)
                        .getDeviceProtocolDialect()
                        .getCustomPropertySet().ifPresent(deviceProtocolCustomPropSet -> {
                    String relationTableName = deviceProtocolCustomPropSet.getPersistenceSupport().tableName();
                    protocolDialect
                            .getProtocolDialect()
                            .getPropertySpecs()
                            .stream()
                            .map(propertySpec -> injector.getInstance(ProtocolDialectDynamicSearchableProperty.class)
                                    .init(this, propertiesGroup, propertySpec, protocolDialectConstriction.get().getConstrainingProperty(), protocolDialect, relationTableName))
                            .forEach(dynamicProperties::add);
                });
            }
            return dynamicProperties;
        }
        return Collections.emptyList();
    }

    private List<SearchableProperty> getGeneralAttributesDynamicProperties(Collection<SearchablePropertyConstriction> constrictions) {
        Optional<SearchablePropertyConstriction> deviceTypeConstriction = constrictions
                .stream()
                .filter(p -> DeviceTypeSearchableProperty.PROPERTY_NAME.equals(p.getConstrainingProperty().getName()))
                .findFirst();
        if (deviceTypeConstriction.isPresent()) {
            DataModel injector = this.deviceDataModelService.dataModel();
            SearchablePropertyGroup propertiesGroup = injector.getInstance(GeneralAttributesDynamicSearchableGroup.class);
            List<SearchableProperty> dynamicProperties = new ArrayList<>();
            Set<Long> uniquePluggableClasses = new HashSet<>();
            for (Object value : deviceTypeConstriction.get().getConstrainingValues()) {
                Optional<DeviceProtocolPluggableClass> pluggableClass = ((DeviceType) value).getDeviceProtocolPluggableClass();
                if (!pluggableClass.isPresent() || !uniquePluggableClasses.add(pluggableClass.get().getId())) {
                    continue;
                }
                for (PropertySpec propertySpec : pluggableClass.get().getDeviceProtocol().getPropertySpecs()) {
                    dynamicProperties.add(injector.getInstance(GeneralAttributeDynamicSearchableProperty.class)
                            .init(this, propertiesGroup, propertySpec, deviceTypeConstriction.get()
                                    .getConstrainingProperty(), pluggableClass.get()));
                }
            }
            return dynamicProperties;
        }
        return Collections.emptyList();
    }

    private List<SearchableProperty> getConnectionDynamicProperties(Collection<SearchablePropertyConstriction> constrictions) {
        Optional<SearchablePropertyConstriction> connectionMethodConstriction = constrictions
                .stream()
                .filter(p -> ConnectionMethodSearchableProperty.PROPERTY_NAME.equals(p.getConstrainingProperty().getName()))
                .findFirst();
        if (connectionMethodConstriction.isPresent()) {
            DataModel injector = this.deviceDataModelService.dataModel();
            SearchablePropertyGroup propertiesGroup = injector.getInstance(ConnectionDynamicSearchableGroup.class);
            List<SearchableProperty> dynamicProperties = new ArrayList<>();
            Set<Long> uniquePluggableClasses = new HashSet<>();
            for (Object value : connectionMethodConstriction.get().getConstrainingValues()) {
                if (value instanceof ConnectionMethodSearchableProperty.ConnectionMethodInfo) {
                    ConnectionTypePluggableClass pluggableClass = ((ConnectionMethodSearchableProperty.ConnectionMethodInfo) value).ctpClass;
                    if (!uniquePluggableClasses.add(pluggableClass.getId())) {
                        continue;
                    }
                    for (PropertySpec propertySpec : pluggableClass.getPropertySpecs()) {
                        dynamicProperties.add(injector.getInstance(ConnectionDynamicSearchableProperty.class)
                                .init(this, propertiesGroup, propertySpec, connectionMethodConstriction.get()
                                        .getConstrainingProperty(), pluggableClass));
                    }
                }
            }
            return dynamicProperties;
        }
        return Collections.emptyList();
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        if (constrictions == null || constrictions.isEmpty()) {
            return getProperties();
        }
        return addDynamicProperties(getProperties(), constrictions);
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
    public Finder<Device> finderFor(List<SearchablePropertyCondition> conditions) {
        return new DeviceFinder(
                new DeviceSearchSqlBuilder(this.deviceDataModelService.dataModel(), conditions, this.clock.instant()),
                this.deviceDataModelService.dataModel());
    }

}