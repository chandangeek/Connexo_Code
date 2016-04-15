package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;

@ProviderType
public interface FullySpecifiedReadingType extends ReadingTypeRequirement {

    ReadingType getReadingType();
}
