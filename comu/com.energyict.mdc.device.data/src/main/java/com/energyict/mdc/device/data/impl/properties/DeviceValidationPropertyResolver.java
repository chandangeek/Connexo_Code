/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.CachedValidationPropertyProvider;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationPropertyProvider;
import com.elster.jupiter.validation.ValidationPropertyResolver;
import com.energyict.mdc.device.data.ChannelValidationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.DeviceValidation;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;
import java.util.Set;

@Component(
        name = "com.energyict.mdc.device.data.impl.properties.DeviceValidationPropertyResolver",
        service = ValidationPropertyResolver.class,
        immediate = true
)
@SuppressWarnings("unused")
public class DeviceValidationPropertyResolver implements ValidationPropertyResolver {

    private DeviceService deviceService;

    public DeviceValidationPropertyResolver() {
    }

    public DeviceValidationPropertyResolver(DeviceService deviceService) {
        this();
        setDeviceService(deviceService);
    }

    @Override
    public Optional<ValidationPropertyProvider> resolve(ChannelsContainer channelsContainer) {
        return channelsContainer.getMeter()
                .flatMap(this::toDevice)
                .map(Device::forValidation)
                .map(deviceValidation -> toValidationPropertyProvider(deviceValidation, channelsContainer));
    }

    private Optional<Device> toDevice(EndDevice endDevice) {
        long deviceId = Long.parseLong(endDevice.getAmrId());
        return deviceService.findDeviceById(deviceId);
    }

    private ValidationPropertyProvider toValidationPropertyProvider(DeviceValidation deviceValidation, ChannelsContainer channelsContainer) {
        Set<ReadingType> readingTypesOfInterest = channelsContainer.getReadingTypes(Range.all());
        CachedValidationPropertyProvider propertyProvider = new CachedValidationPropertyProvider();
        deviceValidation.findAllOverriddenProperties().stream()
                .filter(properties -> readingTypesOfInterest.contains(properties.getReadingType()))
                .forEach(properties -> this.putPropertiesInto(properties, propertyProvider));
        return propertyProvider;
    }

    private void putPropertiesInto(ChannelValidationRuleOverriddenProperties properties, CachedValidationPropertyProvider validationPropertyProvider) {
        validationPropertyProvider.setProperties(
                properties.getReadingType(),
                properties.getValidationRuleName(),
                properties.getValidatorImpl(),
                properties.getValidationAction(),
                properties.getProperties()
        );
    }

    @Override
    public ValidationPropertyDefinitionLevel getLevel() {
        return ValidationPropertyDefinitionLevel.TARGET_OBJECT;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}
