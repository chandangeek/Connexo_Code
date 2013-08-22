package com.elster.jupiter.bootstrap;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

/**
 * Exception to be thrown when a required property was not found.
 *
 * Note that the constructor takes the property key, and not a message.
 *
 */
public class PropertyNotFoundException extends BaseException {
	private static final long serialVersionUID = 1L;

    /**
     * @param propertyKey key of the property
     */
    public PropertyNotFoundException(String propertyKey) {
        super(ExceptionTypes.PROPERTY_NOT_FOUND, MessageFormat.format("Property with key ''{0}'' not found", propertyKey));
        set("propertyKey", propertyKey);
    }
}
