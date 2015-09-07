package com.elster.jupiter.validation.impl;

import java.math.BigDecimal;
import java.util.Objects;

import javax.inject.Inject;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

final class ValidationRulePropertiesImpl implements ValidationRuleProperties {

    private String name;
    private String stringValue;
    private transient PropertySpec propertySpec;

    private Reference<ValidationRule> rule = ValueReference.absent();

    @Inject
    ValidationRulePropertiesImpl() {
    }

    ValidationRulePropertiesImpl init(ValidationRuleImpl rule, String name, Object value) {
        return init(rule, rule.getPropertySpec(name), value);
    }

    ValidationRulePropertiesImpl init(ValidationRule rule, PropertySpec propertySpec, Object value) {
        this.rule.set(rule);
        this.name = propertySpec.getName();
        this.propertySpec = propertySpec;
        setValue(value);
        return this;
    }
    
    PropertySpec getPropertySpec() {
    	if (propertySpec == null) {
    		propertySpec = ((IValidationRule) rule.get()).getPropertySpec(name);
    	}
    	return propertySpec;
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
        return getPropertySpec().getValueFactory().fromStringValue(stringValue);
    }

    @Override
    public void setValue(Object value) {
        if (BigDecimal.class.equals(getPropertySpec().getValueFactory().getValueType())) {
            this.stringValue = value != null ? toStringValue(new BigDecimal(value.toString())) : null;
            return;
        }
        this.stringValue = toStringValue(value);
    }

    @SuppressWarnings("unchecked")
    private String toStringValue(Object object) {
        return getPropertySpec().getValueFactory().toStringValue(object);
    }

    @Override
    public String toString() {
        return getName() + ": " + getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ValidationRulePropertiesImpl that = (ValidationRulePropertiesImpl) o;

        return getRule().getId() == that.getRule().getId() && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
