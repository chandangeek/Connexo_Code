package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

/**
 * Provides functionality to generate exceptions based on the parsing of some data
 *
 * @author khe
 * @since 28/03/12 - 15:12
 */
public class GeneralParseException extends ComServerRuntimeException {

    /**
     * Creates exception based on a given Exception that is thrown when parsing (e.g. Base64 or XML file) goes wrong
     *
     * @param cause the cause of the exception
     */
    public GeneralParseException(final Exception cause) {
        super(cause, generateExceptionCodeByReference(CommonExceptionReferences.GENERAL_PARSE_ERROR), cause.getMessage());
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
}
