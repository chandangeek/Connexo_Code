package com.elster.jupiter.metering;

public interface Multiplier {
    ReadingType getMeasured();

    ReadingType getCalculated();

    MultiplierType getMultiplierType();
}
