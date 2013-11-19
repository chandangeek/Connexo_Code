package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingType;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 18/11/13
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
public interface ReadingTypeInValidationRule {
    long getId();

    ValidationRule getRule();

    ReadingType getReadingType();
}
