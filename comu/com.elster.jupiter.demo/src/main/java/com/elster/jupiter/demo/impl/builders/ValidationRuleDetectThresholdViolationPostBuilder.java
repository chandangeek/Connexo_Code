/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class ValidationRuleDetectThresholdViolationPostBuilder implements Consumer<ValidationRuleSetVersion> {
    private final int thresholdMax;

    public ValidationRuleDetectThresholdViolationPostBuilder(int thresholdMax) {
        this.thresholdMax = thresholdMax;
    }

    @Override
    public void accept(ValidationRuleSetVersion validationRuleSetVersion) {
        validationRuleSetVersion.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.ThresholdValidator", "Detect threshold violation")
                .withReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .havingProperty("minimum").withValue(BigDecimal.ZERO)
                .havingProperty("maximum").withValue(new BigDecimal(this.thresholdMax))
                .active(true)
                .create();
    }
}
