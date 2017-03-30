/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.exceptions.DuplicateNameException;
import com.energyict.mdc.device.data.impl.configchange.ServerProtocolDialectForConfigChange;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectProperty;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ProtocolDialectProperties} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-31 (08:54)
 */
@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
class ProtocolDialectPropertiesImpl
        extends PersistentNamedObject<ProtocolDialectProperties>
        implements
        PropertyFactory<DeviceProtocolDialect, DeviceProtocolDialectProperty>,
            ProtocolDialectProperties,
            ServerProtocolDialectForConfigChange,
            PersistenceAware {

    private long pluggableClassId;
    private transient PropertyCache<DeviceProtocolDialect, DeviceProtocolDialectProperty> cache;

    private Clock clock;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_REQUIRED + "}")
    private Reference<ProtocolDialectConfigurationProperties> configurationProperties = ValueReference.absent();
    private DeviceProtocolDialectUsagePluggableClass deviceProtocolDialectUsagePluggableClass;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CONNECTION_TASK_PLUGGABLE_CLASS_REQUIRED + "}")
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    private ProtocolPluggableService protocolPluggableService;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    ProtocolDialectPropertiesImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ProtocolPluggableService protocolPluggableService, CustomPropertySetService customPropertySetService) {
        super(ProtocolDialectProperties.class, dataModel, eventService, thesaurus);
        this.customPropertySetService = customPropertySetService;
        this.cache = new PropertyCache<>(this);
        this.clock = clock;
        this.protocolPluggableService = protocolPluggableService;
    }

    public ProtocolDialectPropertiesImpl initialize (Device device, ProtocolDialectConfigurationProperties configurationProperties) {
        this.device.set(device);
        this.configurationProperties.set(configurationProperties);
        this.setName(configurationProperties.getName());
        this.setDeviceProtocolPluggableClassFromConfigurationProperties();
        return this;
    }

    private void setDeviceProtocolPluggableClassFromConfigurationProperties() {
        this.setDeviceProtocolPluggableClass(this.getDeviceProtocolPluggableClass(configurationProperties.get()));
    }

    private DeviceProtocolPluggableClass getDeviceProtocolPluggableClass(ProtocolDialectConfigurationProperties configurationProperties) {
        // if we have this object then we can assume that the pluggableclass is present so it's more or less safe to call the get
        return configurationProperties.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().get();
    }

    DeviceProtocol getDeviceProtocol () {
        return this.getDeviceProtocolPluggableClass(this.getProtocolDialectConfigurationProperties()).getDeviceProtocol();
    }

    private void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocol) {
        this.deviceProtocolPluggableClass = deviceProtocol;
        this.pluggableClassId = deviceProtocol.getId();
    }

    @Override
    public void postLoad() {
        this.setDeviceProtocolPluggableClassFromConfigurationProperties();
    }

    @Override
    public void save() {
        super.save();
        this.saveAllProperties();
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.PROTOCOLDIALECTPROPERTIES;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.PROTOCOLDIALECTPROPERTIES;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.PROTOCOLDIALECTPROPERTIES;
    }

    @Override
    protected void doDelete() {
        this.removeAllProperties();
        getDataMapper().remove(this);
    }

    @Override
    protected void validateDelete() {
        // Nothing to validate for now
    }

    @Override
    public Device getDevice () {
        return this.device.get();
    }

    @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return this.configurationProperties.get();
    }

    @Override
    public String getDeviceProtocolDialectName () {
        if (this.getProtocolDialectConfigurationProperties() == null) {
            return "";
        }
        else {
            return getProtocolDialectConfigurationProperties().getDeviceProtocolDialectName();
        }
    }

    @Override
    public String getName() {
        if (this.configurationProperties.isPresent()) {
            return getProtocolDialectConfigurationProperties().getName();
        }
        else {
            return "";
        }
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return null;
    }

    @Override
    public DeviceProtocolDialectProperty newProperty(String propertyName, Object propertyValue, Instant activeDate) {
        return new DeviceProtocolDialectPropertyImpl(propertyName, propertyValue, Range.all(), this.getPluggableClass(), false);
    }

    private void saveAllProperties() {
        if (this.cache.isDirty()) {
            if (this.getTypedProperties().localSize() == 0) {
                this.removeAllProperties();
            }
            else {
                this.getCustomPropertySet().ifPresent(this::saveAllProperties);
                this.clearPropertyCache();
            }
        }
    }

    private Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return this.getDeviceProtocol()
                .getDeviceProtocolDialects()
                .stream()
                .filter(dialect -> dialect.getDeviceProtocolDialectName().equals(this.getDeviceProtocolDialectName()))
                .map(DeviceProtocolDialect::getCustomPropertySet)
                .flatMap(Functions.asStream())
                .findAny();
    }

    private void saveAllProperties(CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>> customPropertySet) {
        Instant now = this.clock.instant();
        this.customPropertySetService.setValuesFor(customPropertySet, this, this.toCustomPropertySetValues(this.getAllLocalProperties(), now), now);
    }

    private CustomPropertySetValues toCustomPropertySetValues(List<DeviceProtocolDialectProperty> properties, Instant now) {
        CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(now);
        properties.forEach(property -> values.setProperty(property.getName(), property.getValue()));
        return values;
    }

    private void removeAllProperties() {
        this.getCustomPropertySet().ifPresent(cps -> this.customPropertySetService.removeValuesFor(cps, this));
    }

    @Override
    public TypedProperties getTypedProperties() {
        TypedProperties typedProperties = TypedProperties.empty();
        if (this.getProtocolDialectConfigurationProperties() != null) {
            typedProperties = TypedProperties.inheritingFrom(this.getProtocolDialectConfigurationProperties().getTypedProperties());
        }
        typedProperties.setAllProperties(this.getLocalTypedProperties());
        return typedProperties;
    }

    private TypedProperties getLocalTypedProperties() {
        TypedProperties typedProperties = TypedProperties.inheritingFrom(this.getPluggableProperties());
        this.getAllLocalProperties(this.clock.instant())
                .stream()
                .filter(property -> property.getValue() != null)
                .forEach(property ->
                        typedProperties.setProperty(
                                property.getName(),
                                property.getValue()));
        return typedProperties;
    }

    @Override
    public DeviceProtocolDialectUsagePluggableClass getPluggableClass() {
        return getDeviceProtocolDialectUsagePluggableClass();
    }

    private Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(long pluggableClassId) {
        return this.protocolPluggableService.findDeviceProtocolPluggableClass(pluggableClassId);
    }

    private DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass() {
        if (this.deviceProtocolDialectUsagePluggableClass == null) {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.getDeviceProtocolPluggableClass();
            this.deviceProtocolDialectUsagePluggableClass = this.protocolPluggableService.getDeviceProtocolDialectUsagePluggableClass(deviceProtocolPluggableClass, this.getDeviceProtocolDialectName());
        }
        return this.deviceProtocolDialectUsagePluggableClass;
    }

    private DeviceProtocolPluggableClass getDeviceProtocolPluggableClass() {
        if (this.deviceProtocolPluggableClass == null) {
            this.deviceProtocolPluggableClass = this.findDeviceProtocolPluggableClass(pluggableClassId).get();
        }
        return this.deviceProtocolPluggableClass;
    }

    private void clearPropertyCache() {
        this.cache.clear();
    }

    private TypedProperties getPluggableProperties() {
        return this.getPluggableClass().getProperties(this.getPluggablePropertySpecs());
    }

    private List<PropertySpec> getPluggablePropertySpecs() {
        return this.getDeviceProtocolDialectUsagePluggableClass().getDeviceProtocolDialect().getPropertySpecs();
    }

    public List<DeviceProtocolDialectProperty> getProperties() {
        return this.getAllProperties();
    }

    public List<DeviceProtocolDialectProperty> getAllProperties() {
        return this.getAllProperties(this.clock.instant());
    }

    private List<DeviceProtocolDialectProperty> getAllLocalProperties() {
        return this.getAllLocalProperties(this.clock.instant());
    }

    private List<DeviceProtocolDialectProperty> getAllLocalProperties(Instant date) {
        return this.cache.get(date);
    }

    @Override
    public List<DeviceProtocolDialectProperty> loadProperties(Instant date) {
        return this.getCustomPropertySet()
                    .map(cps -> this.getCustomProperties(cps, date))
                    .map(this::toProperties)
                    .orElse(Collections.emptyList());
    }

    private CustomPropertySetValues getCustomProperties(CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>> customPropertySet, Instant effectiveTimestamp) {
        return this.customPropertySetService.getUniqueValuesFor(customPropertySet, this, effectiveTimestamp);
    }

    public DeviceProtocolDialectProperty getProperty(String propertyName) {
        return this.getAllProperties()
                .stream()
                .filter(property -> property.getName().equals(propertyName))
                .findAny()
                .orElse(null);
    }

    protected List<DeviceProtocolDialectProperty> toProperties(CustomPropertySetValues values) {
        return values
                .propertyNames()
                .stream()
                .map(propertyName -> this.newDeviceProtocolDialectProperty(values, propertyName))
                .collect(Collectors.toList());
    }

    private DeviceProtocolDialectPropertyImpl newDeviceProtocolDialectProperty(CustomPropertySetValues values, String propertyName) {
        return new DeviceProtocolDialectPropertyImpl(
                propertyName,
                values.getProperty(propertyName),
                values.getEffectiveRange(),
                this.getPluggableClass());
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        Instant now = this.clock.instant();
        this.getAllProperties(now); // Make sure the cache is loaded to avoid that writing to the cache is reverted when the client will call getTypedProperties right after this call
        this.cache.put(now, propertyName, value);
    }

    @Override
    public void removeProperty(String propertyName) {
        Instant now = this.clock.instant();
        this.getAllProperties(now); // Make sure the cache is loaded to avoid that writing to the cache is reverted when the client will call getTypedProperties right after this call
        this.cache.remove(now, propertyName);
    }

    @Override
    public List<DeviceProtocolDialectProperty> getAllProperties(Instant date) {
        List<DeviceProtocolDialectProperty> allProperties = new ArrayList<>();
        List<DeviceProtocolDialectProperty> localProperties = this.getAllLocalProperties(date);
        this.addConfigurationProperties(allProperties, localProperties);
        return allProperties;
    }

    private void addConfigurationProperties(List<DeviceProtocolDialectProperty> allProperties, List<DeviceProtocolDialectProperty> localProperties) {
        final ProtocolDialectConfigurationProperties configurationProperties = this.getProtocolDialectConfigurationProperties();
        if (configurationProperties != null) {
            TypedProperties inheritedProperties = configurationProperties.getTypedProperties();
            inheritedProperties
                    .propertyNames()
                    .stream()
                    // If the inherited property is overruled by a local property, then do not add it
                    .filter(inheritedPropertyName -> !this.propertySpecifiedIn(localProperties, inheritedPropertyName))
                    .map(inheritedPropertyName -> this.newInheritedPropertyFor(inheritedPropertyName, inheritedProperties.getProperty(inheritedPropertyName)))
                    .forEach(allProperties::add);
        }
        allProperties.addAll(localProperties);
    }

    private DeviceProtocolDialectProperty newInheritedPropertyFor (String propertyName, Object propertyValue) {
        return new DeviceProtocolDialectPropertyImpl(propertyName, propertyValue, Range.all(), this.getPluggableClass(), true);
    }

    /**
     * Method to check if the given list of {@link DeviceProtocolDialectProperty DeviceProtocolDialectProperties} contains a property with the specified name.
     *
     * @param properties    The list of DeviceProtocolProperties
     * @param propertyName  The property name to be searched for
     * @return true if the list contains a property with the specified name
     *         false if not the case
     */
    private boolean propertySpecifiedIn(List<DeviceProtocolDialectProperty> properties, String propertyName) {
        for (DeviceProtocolDialectProperty dialectProperty : properties) {
            if (dialectProperty.getName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void setNewProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties newProtocolDialectConfigurationProperties) {
        this.configurationProperties.set(newProtocolDialectConfigurationProperties);
        this.update();
    }

}