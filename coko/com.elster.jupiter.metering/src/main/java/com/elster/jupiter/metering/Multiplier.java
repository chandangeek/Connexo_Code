package com.elster.jupiter.metering;

import java.util.Optional;

public interface Multiplier {
    ReadingType getMeasured();

    Optional<ReadingType> getCalculated();

    MultiplierType getMultiplierType();
}
