package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

public interface FullySpecifiedReadingType extends ReadingTypeRequirement {
    String TYPE_IDENTIFIER = "F";

    ReadingType getReadingType();
}
