/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;

public interface IReadingType extends ReadingType {
    ReadingTypeCodeBuilder builder();

    Quantity toQuantity(BigDecimal value);
}
