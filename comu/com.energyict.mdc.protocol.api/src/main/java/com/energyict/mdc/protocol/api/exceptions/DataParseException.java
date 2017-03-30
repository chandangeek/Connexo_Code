/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.protocol.api.MessageSeeds;

import java.io.IOException;

/**
 * Provides functionality to generate exceptions based on the parsing of some data.
 *
 * @author gna
 * @since 28/03/12 - 15:12
 */
public class DataParseException extends ComServerRuntimeException {

    protected DataParseException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }

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


    /**
     * Creates exception based on an IndexOutOfBoundsException
     *
     * @param cause the cause of the exception
     */
    public static DataParseException indexOutOfBoundsException(final IndexOutOfBoundsException cause) {
        return new DataParseException(cause, MessageSeeds.INDEX_OUT_OF_BOUND_DATA_PARSE_EXCEPTION);
    }


    /**
     * Creates an exception based on the given IOException.
     * It most probably indicates that the DeviceProtocol would try to parse some meterData,
     * but couldn't so the task should be ended.
     *
     * @param cause the cause of the error
     */
    public static DataParseException ioException(final IOException cause) {
        return new DataParseException(cause, MessageSeeds.PROTOCOL_IO_PARSE_ERROR);
    }


    /**
     * Creates an exception based on the given Exception.
     * It should be thrown when parsing of data (e.g. Base64 or XML file) goes wrong.
     *
     * @param cause the cause of the error
     */
    public static DataParseException generalParseException(final Exception cause) {
        return new DataParseException(cause, MessageSeeds.GENERAL_PARSE_EXCEPTION);
    }
}