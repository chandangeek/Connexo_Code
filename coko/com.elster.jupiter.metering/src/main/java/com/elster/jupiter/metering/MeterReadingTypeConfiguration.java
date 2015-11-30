package com.elster.jupiter.metering;

import java.util.OptionalInt;
import java.util.OptionalLong;

public interface MeterReadingTypeConfiguration extends Multiplier {

    OptionalLong getOverflowValue();

    OptionalInt getNumberOfFractionDigits();
}
