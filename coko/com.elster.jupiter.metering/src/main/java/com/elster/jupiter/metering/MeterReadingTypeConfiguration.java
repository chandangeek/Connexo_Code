/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalInt;

public interface MeterReadingTypeConfiguration extends MultiplierUsage {

    Optional<BigDecimal> getOverflowValue();

    OptionalInt getNumberOfFractionDigits();
}
