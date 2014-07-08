package com.elster.jupiter.validation;

import com.elster.jupiter.util.units.Quantity;

public interface ValidationRuleProperties {
    String getName();

    String getDisplayName();

    Quantity getValue();

    void setValue(Quantity value);

    ValidationRule getRule();
}
