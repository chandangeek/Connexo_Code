/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.energyict.mdc.device.data.Device;

public class DeviceValidationRuleSetInfo extends ValidationRuleSetInfo {
    public Boolean isActive;
    public DeviceInfo device;

    public DeviceValidationRuleSetInfo() {
    }

    public DeviceValidationRuleSetInfo(ValidationRuleSet ruleSet, Device device, boolean isActive) {
        super(ruleSet);
        this.isActive = isActive;
        this.device = DeviceInfo.from(device);
    }
}
