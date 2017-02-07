/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;

import java.util.List;

public interface LinkableConfigResolver {

    List<DeviceConfiguration> getLinkableDeviceConfigurations(ValidationRuleSet ruleSet);
    List<DeviceConfiguration> getLinkableDeviceConfigurations(EstimationRuleSet ruleSet);
}
