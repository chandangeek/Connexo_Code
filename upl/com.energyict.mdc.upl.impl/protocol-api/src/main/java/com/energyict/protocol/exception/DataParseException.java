/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exception;

import java.io.IOException;

/**
 * Provides functionality to generate exceptions based on the parsing of some data
 *
 * @author gna
 * @since 28/03/12 - 15:12
 */
public class DataParseException extends com.energyict.protocol.exceptions.DataParseException {

    public DataParseException(Throwable cause, ProtocolExceptionMessageSeeds code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected DataParseException(ProtocolExceptionMessageSeeds reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private DataParseException(ProtocolExceptionMessageSeeds reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }

    /**
     * Creates exception based on an IndexOutOfBoundsException
     *
     * @param cause the cause of the exception
     */
    public static DataParseException indexOutOfBoundsException(final IndexOutOfBoundsException cause) {
        return new DataParseException(cause, ProtocolExceptionMessageSeeds.INDEX_OUT_OF_BOUND_DATA_PARSE_EXCEPTION, cause.getMessage());
    }


    /**
     * Creates an exception based on the given IOException.
     * It most probably indicates that the DeviceProtocol would try to parse some meterData,
     * but couldn't so the task should be ended.
     *
     * @param cause the cause of the error
     */
    public static DataParseException ioException(final IOException cause) {
        return new DataParseException(cause, ProtocolExceptionMessageSeeds.PROTOCOL_IO_PARSE_ERROR, cause.getMessage());
    }


    /**
     * Creates an exception based on the given Exception.
     * It should be thrown when parsing of data (e.g. Base64 or XML file) goes wrong.
     *
     * @param cause the cause of the error
     */
    public static DataParseException generalParseException(final Exception cause) {
        return new DataParseException(cause, ProtocolExceptionMessageSeeds.GENERAL_PARSE_EXCEPTION, cause.getMessage());
    }
}
