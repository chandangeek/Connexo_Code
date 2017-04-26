/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRuleBuilder;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ValidationRuleDetectThresholdViolationPostBuilder implements Consumer<ValidationRuleSetVersion> {
    private final int thresholdMax;
    private List<String> readingTypes = new ArrayList<>();

    public ValidationRuleDetectThresholdViolationPostBuilder(int thresholdMax) {
        this.thresholdMax = thresholdMax;
    }

    @Override
    public void accept(ValidationRuleSetVersion validationRuleSetVersion) {
        ValidationRuleBuilder validationRuleBuilder = validationRuleSetVersion.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.ThresholdValidator", "Detect threshold violation");
        readingTypes.stream()
                .forEach(validationRuleBuilder::withReadingType);
        validationRuleBuilder
                .havingProperty("minimum").withValue(BigDecimal.ZERO)
                .havingProperty("maximum").withValue(new BigDecimal(this.thresholdMax))
                .active(true)
                .create();
    }

    public ValidationRuleDetectThresholdViolationPostBuilder withReadingType(String readingType) {
        readingTypes.add(readingType);
        return this;
    }
}
