/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.estimation.CachedEstimationPropertyProvider;
import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationPropertyProvider;
import com.elster.jupiter.estimation.EstimationPropertyResolver;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.ChannelEstimationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceService;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;
import java.util.Set;

@Component(
        name = "com.energyict.mdc.device.data.impl.properties.DeviceEstimationPropertyResolver",
        service = EstimationPropertyResolver.class,
        immediate = true
)
@SuppressWarnings("unused")
public class DeviceEstimationPropertyResolver implements EstimationPropertyResolver {

    private DeviceService deviceService;

    public DeviceEstimationPropertyResolver() {
    }

    public DeviceEstimationPropertyResolver(DeviceService deviceService) {
        this();
        setDeviceService(deviceService);
    }

    @Override
    public Optional<EstimationPropertyProvider> resolve(ChannelsContainer channelsContainer) {
        return channelsContainer.getMeter()
                .flatMap(this::toDevice)
                .map(Device::forEstimation)
                .map(deviceEstimation -> toEstimationPropertyProvider(deviceEstimation, channelsContainer));
    }

    private Optional<Device> toDevice(EndDevice endDevice) {
        long deviceId = Long.parseLong(endDevice.getAmrId());
        return deviceService.findDeviceById(deviceId);
    }

    private EstimationPropertyProvider toEstimationPropertyProvider(DeviceEstimation deviceEstimation, ChannelsContainer channelsContainer) {
        Set<ReadingType> readingTypesOfInterest = channelsContainer.getReadingTypes(Range.all());
        CachedEstimationPropertyProvider propertyProvider = new CachedEstimationPropertyProvider();
        deviceEstimation.findAllOverriddenProperties().stream()
                .filter(properties -> readingTypesOfInterest.contains(properties.getReadingType()))
                .forEach(properties -> this.putPropertiesInto(properties, propertyProvider));
        return propertyProvider;
    }

    private void putPropertiesInto(ChannelEstimationRuleOverriddenProperties properties, CachedEstimationPropertyProvider estimationPropertyProvider) {
        estimationPropertyProvider.setProperties(
                properties.getReadingType(),
                properties.getEstimationRuleName(),
                properties.getEstimatorImpl(),
                properties.getProperties()
        );
    }

    @Override
    public EstimationPropertyDefinitionLevel getLevel() {
        return EstimationPropertyDefinitionLevel.TARGET_OBJECT;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}
