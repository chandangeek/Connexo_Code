package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingType;

public interface ReadingTypeInValidationRule {

    ValidationRule getRule();

    ReadingType getReadingType();
}
