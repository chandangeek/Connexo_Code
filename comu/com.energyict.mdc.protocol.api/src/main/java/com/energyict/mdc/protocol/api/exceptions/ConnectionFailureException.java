package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;
import com.energyict.mdc.protocol.api.ConnectionException;

/**
 * Wraps a {@link ConnectionException}, turning it into a Runtime exception.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since March 25, 2013 (11:30:57)
 */
public class ConnectionFailureException extends CommunicationException {

    public ConnectionFailureException (ConnectionException cause) {
        super(cause, connectionFailureExceptionCode());
    }

    private static ExceptionCode connectionFailureExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.CONNECTION_FAILURE);
    }

}