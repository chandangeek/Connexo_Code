package com.elster.jupiter.validation;

import java.math.BigDecimal;

public interface ValidationRuleProperties {
    String getName();

    BigDecimal getValue();

    ValidationRule getRule();
}
