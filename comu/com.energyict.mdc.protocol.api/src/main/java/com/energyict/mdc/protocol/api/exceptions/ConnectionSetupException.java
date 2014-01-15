package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;
import com.energyict.mdc.protocol.api.ConnectionException;

/**
 * Wraps a connectionException into a ConnectionSetupException.
 * This exception should only be thrown when the setup of the Connection failed.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 10:33
 */
public class ConnectionSetupException extends CommunicationException {

    public ConnectionSetupException(ConnectionException cause) {
        super(cause, connectionSetupExceptionCode());
    }

    private static ExceptionCode connectionSetupExceptionCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.CONNECTION_SETUP_ERROR);
    }
}
