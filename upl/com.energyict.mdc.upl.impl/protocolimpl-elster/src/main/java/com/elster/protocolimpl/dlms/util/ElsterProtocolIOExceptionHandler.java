package com.elster.protocolimpl.dlms.util;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.ProtocolException;

import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;

import java.io.IOException;


public class ElsterProtocolIOExceptionHandler {
    public static ProtocolRuntimeException handle(IOException e, int nbRetries) {
        if (isUnexpectedResponse(e, nbRetries)) {
            //Unexpected problem or response, but we can still communicate with the device
            return createUnexpectedResponseException(e);
        } else {
            //We can no longer communicate with the device
            return connectionCommunicationException(e, nbRetries);
        }
    }

    private static ProtocolRuntimeException connectionCommunicationException(IOException e, int nbRetries) {
        return createNumberOfRetriesReachedException(e, nbRetries + 1);
    }

    private static boolean isUnexpectedResponse(IOException e, int nbRetries) {
        if (e instanceof NestedIOException) {
            Throwable cause = e.getCause();
            if (cause instanceof ProtocolException) {
                return true;
            } else {
                throw connectionCommunicationException(e, nbRetries);
            }
        } else if (e instanceof ProtocolException) {
            return true;
        } else {
            throw connectionCommunicationException(e, nbRetries);
        }
    }

    protected static ProtocolRuntimeException createUnexpectedResponseException(IOException e) {
        return CommunicationException.unexpectedResponse(e);
    }

    protected static ProtocolRuntimeException createNumberOfRetriesReachedException(IOException e, int nbRetries) {
        return ConnectionCommunicationException.numberOfRetriesReached(e, nbRetries);
    }

}
