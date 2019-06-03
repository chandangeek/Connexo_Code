/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Comparator;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationRuleSetInfo extends com.elster.jupiter.validation.rest.ValidationRuleSetInfo {

    public VersionInfo<Long> parent;
    public Boolean isValidationRuleSetActive = false;

    public ValidationRuleSetInfo() {
        super();
    }

    public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet, DeviceConfiguration deviceConfiguration, boolean isValidationRuleSetActive) {
        super(validationRuleSet);
        this.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        this.isValidationRuleSetActive = isValidationRuleSetActive;
    }

    public static Comparator<ValidationRuleSetInfo> VALIDATION_RULESET_NAME_COMPARATOR = (ruleset1, ruleset2) -> {
        if (ruleset1 == null || ruleset1.name == null || ruleset2 == null || ruleset2.name == null) {
            throw new IllegalArgumentException("Ruleset information is missed");
        }
        return ruleset1.name.compareToIgnoreCase(ruleset2.name);
    };
}
