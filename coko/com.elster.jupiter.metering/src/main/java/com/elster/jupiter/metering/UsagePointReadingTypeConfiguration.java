package com.elster.jupiter.metering;

public interface UsagePointReadingTypeConfiguration {
    ReadingType getMeasured();

    ReadingType getCalculated();

    MultiplierType getMultiplierType();

}
