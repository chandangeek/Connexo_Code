package com.elster.jupiter.validation.rest;

import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ValidationRuleProperties;

public class ValidationRulePropertyInfo {

    public String name;
    public String key;
    public Object value;
    public Unit unit = Unit.WATT_HOUR;
    public int multiplier = 1;
    public boolean required;


    public ValidationRulePropertyInfo(ValidationRuleProperties validationRuleProperties) {
        name = validationRuleProperties.getDisplayName();
        value = validationRuleProperties.getValue();
        required = validationRuleProperties.getRule().isRequired(validationRuleProperties.getName());
    }

    public ValidationRulePropertyInfo() {
    }
}
