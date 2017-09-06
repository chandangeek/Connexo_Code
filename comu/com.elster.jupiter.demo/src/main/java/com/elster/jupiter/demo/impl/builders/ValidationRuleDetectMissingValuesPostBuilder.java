/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRuleBuilder;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ValidationRuleDetectMissingValuesPostBuilder implements Consumer<ValidationRuleSetVersion> {
    private List<String> readingTypes = new ArrayList<>();

    @Override
    public void accept(ValidationRuleSetVersion validationRuleSetVersion) {
        ValidationRuleBuilder validationRuleBuilder = validationRuleSetVersion.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.MissingValuesValidator", "Detect missing values");

        readingTypes.forEach(validationRuleBuilder::withReadingType);

        validationRuleBuilder
                .active(true)
                .create();
    }


    public ValidationRuleDetectMissingValuesPostBuilder withReadingType(String readingType) {
        readingTypes.add(readingType);
        return this;
    }
}
