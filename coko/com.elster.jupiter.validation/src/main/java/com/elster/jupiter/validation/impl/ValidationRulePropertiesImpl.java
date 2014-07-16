package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Objects;

final class ValidationRulePropertiesImpl implements ValidationRuleProperties, PersistenceAware {

    private String name;
    private String stringValue;
    private transient PropertySpec propertySpec;

    private Reference<ValidationRule> rule = ValueReference.absent();

    @Inject
    ValidationRulePropertiesImpl() {
        //for persistence
    }

    ValidationRulePropertiesImpl init(ValidationRule rule, PropertySpec propertySpec, Object value) {
        this.rule.set(rule);
        this.name = propertySpec.getName();
        this.propertySpec = propertySpec;
        setValue(value);
        return this;
    }

    static ValidationRulePropertiesImpl from(DataModel dataModel, ValidationRuleImpl rule, String name, Object value) {
        return dataModel.getInstance(ValidationRulePropertiesImpl.class).init(rule, rule.getPropertySpec(name), value);
    }

    @Override
    public void postLoad() {
        propertySpec = ((IValidationRule) rule.get()).getPropertySpec(name);
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
    public String getDisplayName() {
        return ((ValidationRuleImpl) getRule()).getDisplayName(name);
    }

    @Override
    public Object getValue() {
        return propertySpec.getValueFactory().fromStringValue(stringValue);
    }

    @Override
    public void setValue(Object value) {
        if (BigDecimal.class.equals(propertySpec.getValueFactory().getValueType())) {
            value = new BigDecimal(((Number) value).toString());
        }
        this.stringValue = propertySpec.getValueFactory().toStringValue(value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName()).append(": ").append(getValue());
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) return false;

        ValidationRulePropertiesImpl that = (ValidationRulePropertiesImpl) o;

        return getRule().getId() == that.getRule().getId() && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
