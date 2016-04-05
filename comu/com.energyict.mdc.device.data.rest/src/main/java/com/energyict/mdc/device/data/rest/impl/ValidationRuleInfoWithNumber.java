package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.rest.ValidationRuleInfo;

/**
 * Copyrights EnergyICT
 * Date: 24.03.16
 * Time: 14:26
 */
public class ValidationRuleInfoWithNumber {
    public ValidationRuleInfo key;
    public Long value;

    public ValidationRuleInfoWithNumber() {
    }

    public ValidationRuleInfoWithNumber(ValidationRuleInfo key, Long value) {
        this.key = key;
        this.value = value;
    }
}
