package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ReadingTypeInValidationRule {

    ValidationRule getRule();

    ReadingType getReadingType();
}
