package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

import java.io.IOException;

/**
 * Provides functionality to generate exceptions based on the parsing of some data.
 *
 * @author gna
 * @since 28/03/12 - 15:12
 */
public class DataParseException extends ComServerRuntimeException {

    /**
     * Creates exception based on an IndexOutOfBoundsException
     *
     * @param cause the cause of the exception
     */
    public DataParseException(IndexOutOfBoundsException cause, MessageSeed messageSeed) {
        super(cause, messageSeed, cause.getMessage());
    }

    /**
     * Creates an exception based on the given IOException.
     * It most probably indicates that the DeviceProtocol would try to parse some meterData,
     * but couldn't so the task should be ended.
     *
     * @param cause the cause of the error
     */
    public DataParseException(IOException cause, MessageSeed messageSeed) {
        super(cause, messageSeed, cause.getMessage());
    }

}