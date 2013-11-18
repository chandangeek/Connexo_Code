package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

import java.math.BigDecimal;

public final class ValidationRulePropertiesImpl implements ValidationRuleProperties {

    private String name;
    private BigDecimal value;
    private long ruleId;

    private transient ValidationRule rule;

    private ValidationRulePropertiesImpl() {}     //for persistence

    public ValidationRulePropertiesImpl(ValidationRule rule, String name, BigDecimal value) {
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
    public BigDecimal getValue() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationRulePropertiesImpl that = (ValidationRulePropertiesImpl) o;

        if (ruleId != that.ruleId) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (int) (ruleId ^ (ruleId >>> 32));
        return result;
    }
}
