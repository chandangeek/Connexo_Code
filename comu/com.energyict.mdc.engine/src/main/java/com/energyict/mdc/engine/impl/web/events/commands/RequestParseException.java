package com.energyict.mdc.engine.impl.web.events.commands;

/**
 * Models the exceptional situation that occurs when a String
 * could not be parsed into a {@link Request}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (08:58)
 */
public class RequestParseException extends Exception {

    public RequestParseException (String message) {
        super(message);
    }

    public RequestParseException (String message, Throwable cause) {
        super(message, cause);
    }

}