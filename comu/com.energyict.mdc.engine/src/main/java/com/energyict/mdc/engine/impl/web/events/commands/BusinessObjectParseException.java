package com.energyict.mdc.engine.impl.web.events.commands;

/**
 * Models the exceptional situation that occurs when
 * a parameter of a {@link Request} that is expected
 * to be the unique identifier of a business object
 * fails to parse
 *
 * @author sva
 * @since 25/05/2016 - 9:45
 */
class BusinessObjectParseException extends RequestParseException {
    BusinessObjectParseException(String message, Throwable cause) {
        super(message, cause);
    }
}