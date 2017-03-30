/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import java.util.function.Consumer;

public class ValidationRuleRegisterIncreasePostBuilder implements Consumer<ValidationRuleSetVersion> {
    @Override
    public void accept(ValidationRuleSetVersion validationRuleSetVersion) {
        validationRuleSetVersion.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.RegisterIncreaseValidator", "Register increase")
                .withReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0")
                .withReadingType("0.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0")
                .withReadingType("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0")
                .withReadingType("0.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0")
                .havingProperty("failEqualData").withValue(false)
                .active(true)
                .create();
    }
}
