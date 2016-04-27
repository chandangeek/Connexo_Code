package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface FullySpecifiedReadingTypeRequirement extends ReadingTypeRequirement {
    ReadingType getReadingType();
}