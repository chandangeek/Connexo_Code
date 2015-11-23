package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;

public class UsagePointValidationRuleSetInfo extends ValidationRuleSetInfo {
    public Boolean isActive;
    public UsagePointInfo usagePoint;

    public UsagePointValidationRuleSetInfo() {
    }

    public UsagePointValidationRuleSetInfo(ValidationRuleSet ruleset, boolean isActive) {
        super(ruleset);
        this.isActive = isActive;
    }
}
