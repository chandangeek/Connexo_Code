/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.device.config.validationruleSetResolver", service = ValidationRuleSetResolver.class)
public class DeviceConfigValidationRuleSetResolver implements ValidationRuleSetResolver {
    private volatile DeviceService deviceService;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public DeviceConfigValidationRuleSetResolver() {
        // for OSGi
    }

    @Inject
    public DeviceConfigValidationRuleSetResolver(DeviceService deviceService, DeviceConfigurationService deviceConfigurationService) {
        setDeviceService(deviceService);
        setDeviceConfigurationService(deviceConfigurationService);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public Map<ValidationRuleSet, RangeSet<Instant>> resolve(ValidationContext validationContext) {
        if (hasMdcMeter(validationContext.getMeter()) && validationContext.getMeter().get().getLifecycleDates().getReceivedDate().isPresent()) {
            return deviceService
                    .findDeviceById(Long.valueOf(validationContext.getMeter().get().getAmrId()))
                    .map(device -> device.getDeviceConfiguration().getValidationRuleSets().stream()
                            .collect(Collectors.toMap(Function.identity(), e -> {
                                RangeSet<Instant> rangeSet = TreeRangeSet.create();
                                rangeSet.add(Range.atLeast(validationContext.getMeter().get().getLifecycleDates().getReceivedDate().get()));
                                return rangeSet;
                            }, (a, b) -> a)))
                    .orElse(Collections.emptyMap());
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
        return !deviceConfigurationService.findDeviceConfigurationsForValidationRuleSet(ruleset.getId()).isEmpty();
    }

    @Override
    public boolean isValidationRuleSetActiveOnDeviceConfig(long validationRuleSetId, long deviceId) {
        Optional<Device> device = deviceService.findDeviceById(deviceId);
        if (device.isPresent()) {
            long deviceConfigId = device.get().getDeviceConfiguration().getId();
            return deviceConfigurationService.isValidationRuleSetActiveOnDeviceConfig(validationRuleSetId, deviceConfigId);
        }
        return false;
    }

    @Override
    public boolean canHandleRuleSetStatus() {
        return true;
    }

    private boolean hasMdcMeter(Optional<Meter> koreMeter) {
        return koreMeter.isPresent() && koreMeter.get().getAmrSystem().is(KnownAmrSystem.MDC);
    }
}
