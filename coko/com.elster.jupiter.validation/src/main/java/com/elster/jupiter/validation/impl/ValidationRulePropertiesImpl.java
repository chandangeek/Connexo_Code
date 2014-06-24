package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

import javax.inject.Inject;

final class ValidationRulePropertiesImpl implements ValidationRuleProperties {

    private String name;
    private Quantity value;

    private Reference<ValidationRule> rule = ValueReference.absent();

    @Inject
    ValidationRulePropertiesImpl() {
        //for persistence
    }

    ValidationRulePropertiesImpl init(ValidationRule rule, String name, Quantity value) {
        this.rule.set(rule);
        this.name = name;
        this.value = value;
        return this;
    }

    static ValidationRulePropertiesImpl from(DataModel dataModel, ValidationRule rule, String name, Quantity value) {
        return dataModel.getInstance(ValidationRulePropertiesImpl.class).init(rule, name, value);
    }

    @Override
    public ValidationRule getRule() {
        return rule.get();
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
    public void setValue(Quantity value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName()).append(": ").append(getValue());
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationRulePropertiesImpl that = (ValidationRulePropertiesImpl) o;

        return getRule().getId() == that.getRule().getId() && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        long ruleId = getRule().getId();
        int result = name.hashCode();
        result = 31 * result + (int) (ruleId ^ (ruleId >>> 32));
        return result;
    }
}
