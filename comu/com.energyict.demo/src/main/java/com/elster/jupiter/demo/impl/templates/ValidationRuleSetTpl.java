/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.ValidationRuleSetBuilder;
import com.elster.jupiter.validation.ValidationRuleSet;

public enum ValidationRuleSetTpl implements Template<ValidationRuleSet, ValidationRuleSetBuilder> {

    RESIDENTIAL_CUSTOMERS("Residential electricity", "Set with rules regarding residential electricity customers"),
    RESIDENTIAL_GAS("Residential gas", "Set with rules regarding residential gas customers"),
    RESIDENTIAL_WATER("Residential water", "Set with rules regarding residential water customers");

    private String name;
    private String description;

    ValidationRuleSetTpl(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public Class<ValidationRuleSetBuilder> getBuilderClass() {
        return ValidationRuleSetBuilder.class;
    }

    @Override
    public ValidationRuleSetBuilder get(ValidationRuleSetBuilder builder) {
        return builder.withName(this.name).withDescription(this.description);
    }

    public String getName() {
        return name;
    }
}
