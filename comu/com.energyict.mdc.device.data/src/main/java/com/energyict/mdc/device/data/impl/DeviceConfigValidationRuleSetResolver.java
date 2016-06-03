package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 24/06/2014
 * Time: 17:56
 */
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
    public List<ValidationRuleSet> resolve(ChannelsContainer channelsContainer) {
        if (hasMdcMeter(channelsContainer)) {
            return deviceService
                    .findDeviceById(Long.valueOf(channelsContainer.getMeter().get().getAmrId()))
                    .map(device -> device.getDeviceConfiguration().getValidationRuleSets())
                    .orElse(Collections.emptyList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
        return !deviceConfigurationService.findDeviceConfigurationsForValidationRuleSet(ruleset.getId()).isEmpty();
    }

    private boolean hasMdcMeter(ChannelsContainer channelsContainer) {
        return channelsContainer.getMeter().isPresent() && channelsContainer.getMeter().get().getAmrSystem().is(KnownAmrSystem.MDC);
    }
}
