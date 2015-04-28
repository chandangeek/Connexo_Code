package com.energyict.mdc.device.data.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.Priority;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeterActivation;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;
import com.energyict.mdc.device.data.DeviceService;

@Component(name = "com.energyict.mdc.device.data.EstimationRuleSetResolver", service = EstimationResolver.class)
public class DeviceConfigEstimationRuleSetResolver implements EstimationResolver {
    
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
    public List<EstimationRuleSet> resolve(MeterActivation meterActivation) {
        if (hasMdcMeter(meterActivation)) {
            DeviceEstimation deviceEstimation = deviceService
                    .findDeviceById(Long.valueOf(meterActivation.getMeter().get().getAmrId()))
                    .map(device -> device.forEstimation())
                    .orElse(null);
            if (deviceEstimation != null && deviceEstimation.isEstimationActive()) {
                return deviceEstimation.getEstimationRuleSetActivations().stream()
                        .filter(DeviceEstimationRuleSetActivation::isActive)
                        .map(DeviceEstimationRuleSetActivation::getEstimationRuleSet)
                        .collect(Collectors.toList());
            }

        }
        return Collections.emptyList();
    }

    @Override
    public boolean isInUse(EstimationRuleSet estimationRuleSet) {
        return !deviceConfigurationService.findDeviceConfigurationsForEstimationRuleSet(estimationRuleSet).isEmpty();
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }
    
    private boolean hasMdcMeter(MeterActivation meterActivation) {
        return meterActivation.getMeter().isPresent() && meterActivation.getMeter().get().getAmrSystem().is(KnownAmrSystem.MDC);
    }
}
