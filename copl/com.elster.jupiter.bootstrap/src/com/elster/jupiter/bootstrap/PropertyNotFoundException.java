package com.elster.jupiter.bootstrap;

import java.text.MessageFormat;

/**
 * Exception to be thrown when a required property was not found.
 *
 * Note that the constructor takes the property key, and not a message.
 *
 * Copyrights EnergyICT
 * Date: 21/05/13
 * Time: 11:32
 */
public class PropertyNotFoundException extends RuntimeException {

    /**
     * @param propertyKey key of the property
     */
    public PropertyNotFoundException(String propertyKey) {
        super(MessageFormat.format("Property with key ''{0}'' not found", propertyKey));
    }
}
