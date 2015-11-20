package com.elster.jupiter.metering;

import java.util.OptionalInt;
import java.util.OptionalLong;

public interface MeterReadingTypeConfiguration {
    ReadingType getMeasured();

    ReadingType getCalculated();

    MultiplierType getMultiplierType();

    OptionalLong getOverflowValue();

    OptionalInt getNumberOfFractionDigits();
}
