package com.elster.jupiter.metering;

import java.util.OptionalInt;
import java.util.OptionalLong;

public interface MeterReadingTypeConfiguration extends MultiplierUsage {

    OptionalLong getOverflowValue();

    OptionalInt getNumberOfFractionDigits();
}
