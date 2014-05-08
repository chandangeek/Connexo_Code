package com.energyict.mdc.engine.impl.web.events.commands;

/**
 * Models the exceptional situation that occurs when
 * a String could not be converted to an event {@link com.energyict.mdc.engine.events.Category}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (13:45)
 */
public class UnknownCategoryParseException extends RequestParseException {

    public UnknownCategoryParseException (String eventCategoryName) {
        super("Unrecognized event category name " + eventCategoryName);
    }

}