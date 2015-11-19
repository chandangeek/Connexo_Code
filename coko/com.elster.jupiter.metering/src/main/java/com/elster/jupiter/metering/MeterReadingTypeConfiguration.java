package com.elster.jupiter.metering;

import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Copyrights EnergyICT
 * Date: 18/11/2015
 * Time: 9:47
 */
public interface MeterReadingTypeConfiguration {
    ReadingType getMeasured();

    ReadingType getCalculated();

    MultiplierType getMultiplierType();

    OptionalLong getOverflowValue();

    OptionalInt getNumberOfFractionDigits();
}
