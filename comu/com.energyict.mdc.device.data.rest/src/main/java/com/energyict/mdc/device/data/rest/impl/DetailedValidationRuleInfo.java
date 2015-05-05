package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

/**
 * Created by adrianlupan on 4/22/15.
 */
public class DetailedValidationRuleInfo extends ValidationRuleInfo {

    public long total;

    public DetailedValidationRuleInfo(ValidationRule rule, Long total) {
        super(rule);
        this.total = total;
    }

    public DetailedValidationRuleInfo() {

    }
}
