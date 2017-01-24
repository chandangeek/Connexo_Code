package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.ValidationRuleSetBuilder;
import com.elster.jupiter.validation.ValidationRuleSet;

public enum ValidationRuleSetTpl implements Template<ValidationRuleSet, ValidationRuleSetBuilder> {

    RESIDENTIAL_CUSTOMERS("Residential customers", "Set with rules regarding residential customers"),
    RESIDENTIAL_CUSTOMERS_STRICT("Residential customers strict", "Set with strict rules regarding residential customers");

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
