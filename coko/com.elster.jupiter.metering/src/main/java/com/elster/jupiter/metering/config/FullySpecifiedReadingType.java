package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ReadingType;

public interface FullySpecifiedReadingType extends ReadingTypeRequirement {
    String TYPE_IDENTIFIER = "F";

    ReadingType getReadingType();
}
