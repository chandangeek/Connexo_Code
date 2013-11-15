package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

public class ValidationRulePropertiesImpl implements ValidationRuleProperties {

    private String name;
    private long value;
    private long ruleId;

    private transient ValidationRule rule;

    private ValidationRulePropertiesImpl() {}     //for persistence

    public ValidationRulePropertiesImpl(ValidationRule rule, String name, long value) {
        this.rule = rule;
        this.name = name;
        this.value = value;
        this.ruleId = rule.getId();
    }

    @Override
    public ValidationRule getRule() {
        if (rule == null) {
            rule = Bus.getOrmClient().getValidationRuleFactory().get(ruleId).get();
        }
        return rule;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName()).append(": ").append(getValue());
        return builder.toString();
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }
}
