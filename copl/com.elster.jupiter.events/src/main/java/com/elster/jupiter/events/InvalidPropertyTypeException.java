package com.elster.jupiter.events;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when a property reached through an access path does not have the expected type.
 */
public class InvalidPropertyTypeException extends BaseException {

    public InvalidPropertyTypeException(Object bean, String accessPath, Class<?> expectedType, Class<?> actualType, Thesaurus thesaurus) {
        super(ExceptionTypes.INVALID_PROPERTY_TYPE, buildMessage(thesaurus, bean, accessPath, expectedType, actualType));
        set("bean", bean);
        set("accessPath", accessPath);
        set("expectedType", expectedType);
        set("actualType", actualType);
    }

    private static String buildMessage(Thesaurus thesaurus, Object bean, String accessPath, Class<?> expectedType, Class<?> actualType) {
        return thesaurus.getFormat(MessageSeeds.INVALID_PROPERTY_TYPE).format(accessPath, bean, actualType, expectedType);
    }
}
