package com.elster.jupiter.validation;

public interface ValidationRuleProperties {
    String getName();

    String getDisplayName();

    Object getValue();

    void setValue(Object value);

    ValidationRule getRule();
}
