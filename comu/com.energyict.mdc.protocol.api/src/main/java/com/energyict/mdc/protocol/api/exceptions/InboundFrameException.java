package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

import java.io.IOException;

/**
 * Models the expected but exceptional situations that can occur with frames.
 *
 * Date: 16/10/12
 * Time: 12:01
 * Author: khe
 */
public final class InboundFrameException extends CommunicationException {

    /**
     * Creates an {@link InboundFrameException} indicating that the first received frame is not as expected (e.g. too short, wrong contents)
     *
     * @param frame                 the content of the inbound frame
     * @param additionalInformation E.g. for describing the expected frame format
     *                              if left empty, only "Unexpected inbound frame contents" is shown.
     * @return the newly created exception
     */
    public static InboundFrameException unexpectedFrame (final String frame, final String additionalInformation) {
        return new InboundFrameException(generateExceptionCodeByReference(CommonExceptionReferences.INBOUND_UNEXPECTED_FRAME), frame, additionalInformation);
    }

    /**
     * Creates an {@link InboundFrameException} indicating that the first received frame is not as expected (e.g. too short, wrong contents)
     *
     * @param e The IOException
     * @param frame the content of the inbound frame
     * @param additionalInformation E.g. for describing the expected frame format
     *                              if left empty, only "Unexpected inbound frame contents" is shown.
     * @return the newly created exception
     */
    public static InboundFrameException unexpectedFrame (IOException e, String frame, final String additionalInformation) {
        return new InboundFrameException(e, generateExceptionCodeByReference(CommonExceptionReferences.INBOUND_UNEXPECTED_FRAME), frame, additionalInformation);
    }

    /**
     * Creates an {@link InboundFrameException} indicating that the receiving of the inbound frame timed out
     *
     * @param cause the cause of the timeout
     * @return the newly created exception
     */
    public static InboundFrameException timeout (final String cause) {
        return new InboundFrameException(generateExceptionCodeByReference(CommonExceptionReferences.INBOUND_TIMEOUT), cause);
    }

    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>CommonExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode generateExceptionCodeByReference(CommonExceptionReferences reference) {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, reference);
    }

    private InboundFrameException (ExceptionCode code, Object... messageArguments) {
        super(code, messageArguments);
    }

    private InboundFrameException (Throwable cause, ExceptionCode code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

}