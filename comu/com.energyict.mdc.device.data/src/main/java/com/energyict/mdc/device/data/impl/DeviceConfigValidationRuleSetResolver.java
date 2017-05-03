/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.device.config.validationruleSetResolver", service = ValidationRuleSetResolver.class)
public class DeviceConfigValidationRuleSetResolver implements ValidationRuleSetResolver {

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
    public Map<ValidationRuleSet, List<Range<Instant>>> resolve(ValidationContext validationContext) {
        if (hasMdcMeter(validationContext.getMeter()) && validationContext.getMeter().get().getLifecycleDates().getReceivedDate().isPresent()) {
            return deviceService
                    .findDeviceById(Long.valueOf(validationContext.getMeter().get().getAmrId()))
                    .map(device -> device.getDeviceConfiguration().getValidationRuleSets().stream()
                            .collect(Collectors.toMap(Function.identity(), e ->
                                    (List<Range<Instant>>) (new ArrayList<>(Collections.singletonList(Range.atLeast(validationContext.getMeter().get().getLifecycleDates().getReceivedDate().get())))), (a, b) -> a)))
                    .orElse(Collections.emptyMap());
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
        return !deviceConfigurationService.findDeviceConfigurationsForValidationRuleSet(ruleset.getId()).isEmpty();
    }

    private boolean hasMdcMeter(Optional<Meter> koreMeter) {
        return koreMeter.isPresent() && koreMeter.get().getAmrSystem().is(KnownAmrSystem.MDC);
    }
}
