/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.Priority;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.device.data.EstimationRuleSetResolver", service = EstimationResolver.class)
public class DeviceConfigurationEstimationRuleSetResolver implements EstimationResolver {

    private volatile DeviceService deviceService;
    private volatile DeviceConfigurationService deviceConfigurationService;

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }


    @Override
    public List<EstimationRuleSet> resolve(ChannelsContainer channelsContainer) {
        if (hasMdcMeter(channelsContainer)) {
            return getDeviceForEstimation(channelsContainer.getMeter().get())
                    .filter(DeviceEstimation::isEstimationActive)
                    .map(deviceEstimation -> deviceEstimation.getEstimationRuleSetActivations().stream())
                    .orElseGet(Stream::empty)
                    .filter(DeviceEstimationRuleSetActivation::isActive)
                    .map(DeviceEstimationRuleSetActivation::getEstimationRuleSet)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isInUse(EstimationRuleSet estimationRuleSet) {
        return !deviceConfigurationService.findDeviceConfigurationsForEstimationRuleSet(estimationRuleSet).find().isEmpty();
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public boolean isEstimationActive(Meter meter) {
        return getDeviceForEstimation(meter).map(DeviceEstimation::isEstimationActive).orElse(false);
    }

    private boolean hasMdcMeter(MeterActivation meterActivation) {
        return meterActivation.getMeter().isPresent() && isMdcMeter(meterActivation.getMeter().get());
    }

    private boolean hasMdcMeter(ChannelsContainer channelsContainer) {
        return channelsContainer.getMeter().isPresent() && isMdcMeter(channelsContainer.getMeter().get());
    }

    private boolean isMdcMeter(Meter meter) {
        return meter.getAmrSystem().is(KnownAmrSystem.MDC);
    }

    private Optional<DeviceEstimation> getDeviceForEstimation(Meter meter) {
        if (!isMdcMeter(meter)) {
            return Optional.empty();
        }
        return deviceService.findDeviceById(Long.valueOf(meter.getAmrId())).map(Device::forEstimation);
    }
}
