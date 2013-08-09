package com.elster.jupiter.events;

import java.text.MessageFormat;

public class InvalidPropertyType extends RuntimeException {

    public InvalidPropertyType(Object bean, String accessPath, Class<?> expectedType, Class<?> actualType) {
        super(MessageFormat.format("Access path ''{0}'' on bean ''{1}'' was of type {2} while type {3} was expected.", accessPath, bean, actualType, expectedType));
    }
}
