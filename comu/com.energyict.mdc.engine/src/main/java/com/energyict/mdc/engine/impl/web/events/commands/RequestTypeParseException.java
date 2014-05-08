package com.energyict.mdc.engine.impl.web.events.commands;

/**
 * Models the exceptional situation that occurs when
 * the type of the {@link Request} could not be parsed
 * from a String.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (10:40)
 */
public class RequestTypeParseException extends RequestParseException {

    public RequestTypeParseException (String requestType, int requestTypeOffsetInMessage) {
        super("Unrecognized request type:" + requestType + " at index " + requestTypeOffsetInMessage);
    }

}