package com.elster.jupiter.validation.rest;

import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ValidationRuleProperties;

import java.math.BigDecimal;

public class ValidationRulePropertyInfo {

    public String name;
    public BigDecimal value;
    public Unit unit = Unit.WATT_HOUR;
    public int multiplier = 1;
    public boolean required;


    public ValidationRulePropertyInfo(ValidationRuleProperties validationRuleProperties) {
        name = validationRuleProperties.getName();
        Quantity qty = validationRuleProperties.getValue();
        value = qty.getValue();
        unit = qty.getUnit();
        multiplier = qty.getMultiplier();
        required = validationRuleProperties.getRule().isRequired(name);
    }

    public ValidationRulePropertyInfo() {
    }
}
