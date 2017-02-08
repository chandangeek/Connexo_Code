package com.energyict.protocolimpl.errorhandling;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;

import java.io.IOException;


public class ProtocolIOExceptionHandler {
    public static CommunicationException handle(IOException e, int nbRetries) {
        if (isUnexpectedResponse(e, nbRetries)) {
            //Unexpected problem or response, but we can still communicate with the device
            return CommunicationException.unexpectedResponse(e);
        } else {
            //We can no longer communicate with the device
            return connectionCommunicationException(e, nbRetries);
        }
    }

    private static ConnectionCommunicationException connectionCommunicationException(IOException e, int nbRetries) {
        return  ConnectionCommunicationException.numberOfRetriesReached(e, nbRetries + 1);
    }

    private static boolean isUnexpectedResponse(IOException e, int nbRetries) {
        if (e instanceof ProtocolException) {
            return true;
        } else {
            throw connectionCommunicationException(e, nbRetries);
        }
    }
}
