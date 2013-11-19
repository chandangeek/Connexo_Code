package com.elster.jupiter.validation;

import com.elster.jupiter.util.units.Quantity;

public interface ValidationRuleProperties {
    String getName();

    Quantity getValue();

    ValidationRule getRule();
}
