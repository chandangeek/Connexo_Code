package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

/**
 * Should be used by a DeviceProtocol if for any reason the meter does not respond to any requests anymore.
 *
 * @author gna
 * @since 28/03/12 - 15:39
 */
public class ConnectionTimeOutException extends CommunicationException {

    /**
     * Creates a new ConnectionTimeOutException
     *
     * @param totalNumberOfAttempts the total number of attempts (including the initial one) a certain request is sent, before this exception has been thrown
     */
    public ConnectionTimeOutException(final int totalNumberOfAttempts) {
        super(generateExceptionCodeByReference(CommonExceptionReferences.CONNECTION_TIMEOUT), totalNumberOfAttempts);
    }

    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>ComServerExecutionExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode generateExceptionCodeByReference(CommonExceptionReferences reference) {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, reference);
    }

}