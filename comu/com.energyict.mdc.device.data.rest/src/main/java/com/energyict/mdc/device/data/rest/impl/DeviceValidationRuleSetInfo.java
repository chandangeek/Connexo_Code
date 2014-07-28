package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;

public class DeviceValidationRuleSetInfo extends ValidationRuleSetInfo {
    public Boolean isActive;

    public DeviceValidationRuleSetInfo(ValidationRuleSet ruleset, boolean isActive) {
        super(ruleset);
        this.isActive = isActive;
    }
}
