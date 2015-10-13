package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;

import java.util.List;

/**
 * Implementors of this interface willd determine to which DeviceConfigurations a given validationRuleSet can be added.

 *
 * Copyrights EnergyICT
 * Date: 28/07/2014
 * Time: 10:33
 */
public interface LinkableConfigResolver {

    List<DeviceConfiguration> getLinkableDeviceConfigurations(ValidationRuleSet ruleSet);
    List<DeviceConfiguration> getLinkableDeviceConfigurations(EstimationRuleSet ruleSet);
}
