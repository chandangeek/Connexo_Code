package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
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

    private volatile DeviceDataService deviceDataService;
    private volatile DeviceConfigurationService deviceConfigurationService;

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public List<ValidationRuleSet> resolve(MeterActivation meterActivation) {
        if (hasMdcMeter(meterActivation)) {
            Device device = deviceDataService.findDeviceById(Long.valueOf(meterActivation.getMeter().get().getAmrId()));
            if (device != null) {
                return device.getDeviceConfiguration().getValidationRuleSets();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
        if(!deviceConfigurationService.findDeviceConfigurationsForValidationRuleSet(ruleset.getId()).isEmpty()) {
                return true;
        }
        return false;
    }

    private boolean hasMdcMeter(MeterActivation meterActivation) {
        return meterActivation.getMeter().isPresent() && meterActivation.getMeter().get().getAmrSystem().is(KnownAmrSystem.MDC);
    }
}
