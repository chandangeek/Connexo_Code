package com.elster.jupiter.validation;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

public class MissingRequiredProperty extends BaseException {

    public MissingRequiredProperty(String missingKey) {
        super(ExceptionTypes.MISSING_PROPERTY, MessageFormat.format("Required property with key ''{0}'' was not found.", missingKey));
        set("missingKey", missingKey);
    }
}
