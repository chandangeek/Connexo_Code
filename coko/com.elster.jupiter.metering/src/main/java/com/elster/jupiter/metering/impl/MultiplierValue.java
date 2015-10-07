package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MultiplierType;

import java.math.BigDecimal;

public interface MultiplierValue {
    BigDecimal getValue();

    void setValue(BigDecimal value);

    MultiplierType getType();
}
