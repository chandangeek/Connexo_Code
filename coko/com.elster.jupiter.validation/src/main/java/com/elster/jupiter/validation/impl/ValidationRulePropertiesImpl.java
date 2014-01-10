package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

import javax.inject.Inject;

final class ValidationRulePropertiesImpl implements ValidationRuleProperties {

    private String name;
    private Quantity value;
    private long ruleId;

    private transient ValidationRule rule;

    private final DataModel dataModel;

    @Inject
    ValidationRulePropertiesImpl(DataModel dataModel) {
        //for persistence
        this.dataModel = dataModel;
    }

    ValidationRulePropertiesImpl init(ValidationRule rule, String name, Quantity value) {
        this.rule = rule;
        this.name = name;
        this.value = value;
        this.ruleId = rule.getId();
        return this;
    }

    static ValidationRulePropertiesImpl from(DataModel dataModel, ValidationRule rule, String name, Quantity value) {
        return dataModel.getInstance(ValidationRulePropertiesImpl.class).init(rule, name, value);
    }

    @Override
    public ValidationRule getRule() {
        if (rule == null) {
            rule = dataModel.mapper(ValidationRule.class).getOptional(ruleId).get();
        }
        return rule;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Quantity getValue() {
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
