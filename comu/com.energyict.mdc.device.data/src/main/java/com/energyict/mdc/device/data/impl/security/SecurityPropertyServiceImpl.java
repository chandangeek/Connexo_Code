/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.exceptions.SecurityPropertyException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.configchange.ServerSecurityPropertyServiceForConfigChange;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Provides an implementation for the {@link SecurityPropertyService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (16:25)
 */
@Component(name = "com.energyict.mdc.device.data.security", service = SecurityPropertyService.class, property = "name=SecurityPropertyService")
public class SecurityPropertyServiceImpl implements SecurityPropertyService, ServerSecurityPropertyServiceForConfigChange {

    private volatile Clock clock;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework
    public SecurityPropertyServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public SecurityPropertyServiceImpl(Clock clock, CustomPropertySetService customPropertySetService, NlsService nlsService) {
        this();
        this.setClock(clock);
        this.setCustomPropertySetService(customPropertySetService);
        this.setNlsService(nlsService);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public List<SecurityProperty> getSecurityProperties(Device device, Instant when, SecurityPropertySet securityPropertySet) {
        return this.findActiveProperties(device, securityPropertySet, when)
                .map(values -> this.toSecurityProperties(values, device, securityPropertySet))
                .orElse(Collections.emptyList());
    }

    public Optional<CustomPropertySetValues> findActiveProperties(Device device, SecurityPropertySet securityPropertySet, Instant activeDate) {
        Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> customPropertySet = getDeviceProtocolCustomPropertySet(device);
        if (customPropertySet.isPresent()) {
            return Optional.of(
                    this.customPropertySetService.getUniqueValuesFor(
                            customPropertySet.get(),
                            device,
                            activeDate,
                            securityPropertySet));
        } else {
            return Optional.empty();
        }
    }

    private List<SecurityProperty> toSecurityProperties(CustomPropertySetValues values, Device device, SecurityPropertySet securityPropertySet) {
        return securityPropertySet
                .getPropertySpecs()
                .stream()
                .map(each -> this.toSecurityProperty(each, values, device, securityPropertySet))
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
    }

    private Optional<SecurityProperty> toSecurityProperty(PropertySpec propertySpec, CustomPropertySetValues values, Device device, SecurityPropertySet securityPropertySet) {
        Boolean complete = (Boolean) values.getProperty(CommonBaseDeviceSecurityProperties.Fields.COMPLETE.javaName());
        Object propertyValue = values.getProperty(propertySpec.getName());
        if (propertyValue != null) {
            return Optional.of(
                    new SecurityPropertyImpl(
                            device,
                            securityPropertySet,
                            propertySpec,
                            propertyValue,
                            values.getEffectiveRange(),
                            // Status is a required attribute on the relation should it cannot be null
                            complete));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean hasSecurityProperties(Device device, Instant when, SecurityPropertySet securityPropertySet) {
        return this.findActiveProperties(device, securityPropertySet, when).isPresent();
    }

    @Override
    public boolean securityPropertiesAreValid(Device device, SecurityPropertySet securityPropertySet) {
        if (this.hasRequiredProperties(securityPropertySet)) {
            return !this.isMissingOrIncomplete(device, securityPropertySet);
        } else {
            return true;
        }
    }

    private boolean hasRequiredProperties(SecurityPropertySet securityPropertySet) {
        return securityPropertySet
                .getPropertySpecs()
                .stream()
                .anyMatch(PropertySpec::isRequired);
    }

    private Set<PropertySpec> getAllRequiredProperties(SecurityPropertySet securityPropertySet) {
        return securityPropertySet
                .getPropertySpecs()
                .stream()
                .filter(PropertySpec::isRequired).collect(Collectors.toSet());
    }

    @Override
    public boolean securityPropertiesAreValid(Device device) {
        return device
                .getDeviceConfiguration()
                .getComTaskEnablements()
                .stream()
                .noneMatch(comTaskEnablement -> isMissingOrIncomplete(device, comTaskEnablement.getSecurityPropertySet()));
    }

    private boolean isMissingOrIncomplete(Device device, SecurityPropertySet securityPropertySet) {
        Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> customPropertySet = getDeviceProtocolCustomPropertySet(device);
        return !securityPropertySet.getPropertySpecs().isEmpty() && (!customPropertySet.isPresent() || !this.customPropertySetService.hasValueForPropertySpecs(
                customPropertySet.get(),
                device, this.clock.instant(),
                getAllRequiredProperties(securityPropertySet),
                securityPropertySet
        ));
    }

    @Override
    public void setSecurityProperties(Device device, SecurityPropertySet securityPropertySet, TypedProperties properties) {
        if (securityPropertySet.currentUserIsAllowedToEditDeviceProperties()) {
            this.doSetSecurityProperties(device, securityPropertySet, properties);
        } else {
            throw new SecurityPropertyException(securityPropertySet, this.thesaurus, MessageSeeds.USER_IS_NOT_ALLOWED_TO_EDIT_SECURITY_PROPERTIES);
        }
    }

    private void doSetSecurityProperties(Device device, SecurityPropertySet securityPropertySet, TypedProperties properties) {
        CustomPropertySetValues values = CustomPropertySetValues.emptyDuring(Range.atLeast(this.clock.instant()));
        values.setProperty(CommonBaseDeviceSecurityProperties.Fields.PROPERTY_SPEC_PROVIDER.javaName(), securityPropertySet);
        properties.propertyNames().stream().forEach(propertyName -> values.setProperty(propertyName, properties.getLocalValue(propertyName)));
        values.setProperty(CommonBaseDeviceSecurityProperties.Fields.COMPLETE.javaName(), this.isSecurityPropertySetComplete(securityPropertySet, properties));
        getDeviceProtocolCustomPropertySet(device)
                .ifPresent(cps -> this.customPropertySetService.setValuesFor(
                        cps,
                        device,
                        values,
                        this.clock.instant(),
                        securityPropertySet));
    }

    private boolean isSecurityPropertySetComplete(SecurityPropertySet securityPropertySet, TypedProperties typedProperties) {
        return !securityPropertySet.getPropertySpecs()
                .stream()
                .anyMatch(p -> p.isRequired() && !typedProperties.hasLocalValueFor(p.getName()));
    }

    @Override
    public void deleteSecurityPropertiesFor(Device device) {
        getDeviceProtocolCustomPropertySet(device).ifPresent(cps -> this.deleteSecurityPropertiesFor(device, cps));
    }

    private void deleteSecurityPropertiesFor(Device device, CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>> cps) {
        device
                .getDeviceConfiguration()
                .getSecurityPropertySets()
                .forEach(securitySet -> this.customPropertySetService.removeValuesFor(cps, device, securitySet));
    }

    @Override
    public void updateSecurityPropertiesWithNewSecurityPropertySet(Device device, SecurityPropertySet originSecurityPropertySet, SecurityPropertySet destinationSecurityPropertySet) {
        this.findActiveProperties(device, originSecurityPropertySet, clock.instant())
                .ifPresent(customPropertySetValues -> {
                    final TypedProperties typedProperties = TypedProperties.empty();
                    destinationSecurityPropertySet.getPropertySpecs().stream()
                            .forEach(propertySpec -> typedProperties.setProperty(propertySpec.getName(), customPropertySetValues.getProperty(propertySpec.getName())));
                    deleteSecurityPropertiesFor(device, originSecurityPropertySet);
                    setSecurityProperties(device, destinationSecurityPropertySet, typedProperties);
                });
    }

    @Override
    public void deleteSecurityPropertiesFor(Device device, SecurityPropertySet securityPropertySet) {
        getDeviceProtocolCustomPropertySet(device).ifPresent(baseDeviceCustomPropertySet -> this.customPropertySetService.removeValuesFor(baseDeviceCustomPropertySet, device, securityPropertySet));
    }

    private Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getDeviceProtocolCustomPropertySet(Device device) {
        return device.getDeviceType()
                .getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol()
                        .getCustomPropertySet())
                .orElse(Optional.empty());
    }
}