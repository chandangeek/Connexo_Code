package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

import java.io.IOException;

/**
 * Wrapper around a {@link CommunicationException} indicating that an exception occurred
 * during the <b>setup</b> of an inbound call
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 14:26
 */
public class InboundCommunicationException extends ComServerRuntimeException {

    /**
     * Exception indicating that the <b>setup</b> of the inbound call failed.
     *
     * @param cause the actual cause of the exception
     */
    public InboundCommunicationException(IOException cause) {
        super(cause, unexpectedIoExceptionCode());
    }

    private static ExceptionCode unexpectedIoExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION);
    }
}
