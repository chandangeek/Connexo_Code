/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MultiplierType;

import java.math.BigDecimal;

public interface MultiplierValue {
    BigDecimal getValue();

    void setValue(BigDecimal value);

    MultiplierType getType();
}
