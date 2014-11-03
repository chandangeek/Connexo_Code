package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.ComServerRuntimeException;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Provides functionality to generate exceptions based on the parsing of some data.
 *
 * @author khe
 * @since 28/03/12 - 15:12
 */
public class GeneralParseException extends ComServerRuntimeException {

    /**
     * Creates exception based on a given Exception that is thrown when parsing (e.g. Base64 or XML file) goes wrong.
     *
     * @param cause the cause of the exception
     */
    public GeneralParseException(MessageSeed messageSeed, Exception cause) {
        super(cause, messageSeed, cause.getMessage());
    }

}
