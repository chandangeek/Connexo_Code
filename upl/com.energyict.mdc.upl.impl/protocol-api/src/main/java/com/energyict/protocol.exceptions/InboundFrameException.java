package com.energyict.protocol.exceptions;

import java.io.IOException;

/**
 * Models the expected but exceptional situations that can occur with frames.
 * <p/>
 * Date: 16/10/12
 * Time: 12:01
 * Author: khe
 */
public final class InboundFrameException extends CommunicationException {


    protected InboundFrameException(Throwable cause, ProtocolExceptionReference code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected InboundFrameException(ProtocolExceptionReference reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private InboundFrameException(ProtocolExceptionReference reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }

    /**
     * Creates an {@link InboundFrameException} indicating that the first received frame is not as expected (e.g. too short, wrong contents)
     *
     * @param frame                 the content of the inbound frame
     * @param additionalInformation E.g. for describing the expected frame format
     *                              if left empty, only "Unexpected inbound frame contents" is shown.
     * @return the newly created exception
     */
    public static InboundFrameException unexpectedFrame(final String frame, final String additionalInformation) {
        return new InboundFrameException(ProtocolExceptionReference.INBOUND_UNEXPECTED_FRAME, frame, additionalInformation);
    }

    /**
     * Creates an {@link InboundFrameException} indicating that the first received frame is not as expected (e.g. too short, wrong contents)
     *
     * @param e                     The IOException
     * @param frame                 the content of the inbound frame
     * @param additionalInformation E.g. for describing the expected frame format
     *                              if left empty, only "Unexpected inbound frame contents" is shown.
     * @return the newly created exception
     */
    public static InboundFrameException unexpectedFrame(IOException e, String frame, final String additionalInformation) {
        return new InboundFrameException(e, ProtocolExceptionReference.INBOUND_UNEXPECTED_FRAME, frame, additionalInformation);
    }

    /**
     * Creates an {@link InboundFrameException} indicating that the receiving of the inbound frame timed out
     *
     * @param cause the cause of the timeout
     * @return the newly created exception
     */
    public static InboundFrameException timeoutException(final String cause) {
        return new InboundFrameException(ProtocolExceptionReference.INBOUND_TIMEOUT, cause);
    }

    public static InboundFrameException timeoutException(Exception cause, String message) {
        return  new InboundFrameException(cause, ProtocolExceptionReference.INBOUND_TIMEOUT, message);
    }
}