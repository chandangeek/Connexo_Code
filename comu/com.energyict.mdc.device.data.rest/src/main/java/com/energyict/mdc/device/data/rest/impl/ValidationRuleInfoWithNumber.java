/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.rest.ValidationRuleInfo;

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
