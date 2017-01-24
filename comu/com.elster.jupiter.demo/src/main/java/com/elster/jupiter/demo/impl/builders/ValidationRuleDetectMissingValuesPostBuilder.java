package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import java.util.function.Consumer;

public class ValidationRuleDetectMissingValuesPostBuilder implements Consumer<ValidationRuleSetVersion> {
    @Override
    public void accept(ValidationRuleSetVersion validationRuleSetVersion) {
        validationRuleSetVersion.addRule(ValidationAction.FAIL, "com.elster.jupiter.validators.impl.MissingValuesValidator", "Detect missing values")
                // 15min Electricity
                .withReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .withReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0")
                // Daily Electricity
                .withReadingType("11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0")
                .withReadingType("11.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0")
                .withReadingType("11.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0")
                .withReadingType("11.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0")
                // Monthly Electricity
                .withReadingType("13.0.0.9.1.1.12.0.0.0.0.1.0.0.0.0.72.0")
                .withReadingType("13.0.0.9.1.1.12.0.0.0.0.2.0.0.0.0.72.0")
                .withReadingType("13.0.0.9.19.1.12.0.0.0.0.1.0.0.0.0.72.0")
                .withReadingType("13.0.0.9.19.1.12.0.0.0.0.2.0.0.0.0.72.0")
                .active(true)
                .create();
    }
}
