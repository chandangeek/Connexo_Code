package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

import java.io.IOException;

/**
 * Provides functionality to generate exceptions based on the parsing of some data
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
    public DataParseException(final IndexOutOfBoundsException cause) {
        super(cause, generateExceptionCodeByReference(CommonExceptionReferences.INDEX_OUT_OF_BOUND_DATA_PARSE_EXCEPTION), cause.getMessage());
    }

    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>ComServerExecutionExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode generateExceptionCodeByReference(CommonExceptionReferences reference) {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.DATA_QUALITY, reference);
    }

    /**
     * Creates an exception based on the given IOException.
     * It most probably indicates that the DeviceProtocol would try to parse some meterData,
     * but couldn't so the task should be ended.
     *
     * @param cause the cause of the error
     */
    public DataParseException(final IOException cause){
        super(cause, generateExceptionCodeByReference(CommonExceptionReferences.PROTOCOL_IO_PARSE_ERROR), cause.getMessage());
    }
}
