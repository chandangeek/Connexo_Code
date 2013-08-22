package com.elster.jupiter.events;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

/**
 * Thrown when a property reached through an access path does not have the expected type.
 */
public class InvalidPropertyTypeException extends BaseException {

    public InvalidPropertyTypeException(Object bean, String accessPath, Class<?> expectedType, Class<?> actualType) {
        super(ExceptionTypes.INVALID_PROPERTY_TYPE, MessageFormat.format("Access path ''{0}'' on bean ''{1}'' was of type {2} while type {3} was expected.", accessPath, bean, actualType, expectedType));
        set("bean", bean);
        set("accessPath", accessPath);
        set("expectedType", expectedType);
        set("actualType", actualType);
    }
}
