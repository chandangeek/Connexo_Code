/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

/**
 * Models the exceptional situation that occurs when the String
 * representation of a {@link Request} does not match
 * with the expected format.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (11:23)
 */
class UnexpectedRequestFormatException extends RequestParseException {

    UnexpectedRequestFormatException(String expectedCommandPattern) {
        super("Request does not conform the expected form: " + expectedCommandPattern);
    }

}