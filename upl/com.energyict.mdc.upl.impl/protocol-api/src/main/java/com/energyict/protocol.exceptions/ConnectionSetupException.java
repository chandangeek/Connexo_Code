package com.energyict.protocol.exceptions;

import java.io.IOException;

/**
 * Wraps a connectionException into a ConnectionSetupException.
 * This exception should only be thrown when the setup of the Connection failed.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 10:33
 */
public class ConnectionSetupException extends CommunicationException {

    protected ConnectionSetupException(Throwable cause, ProtocolExceptionReference code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected ConnectionSetupException(ProtocolExceptionReference reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private ConnectionSetupException(ProtocolExceptionReference reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }

    public static ConnectionSetupException connectionSetupFailed(ConnectionException cause) {
        return new ConnectionSetupException(cause, ProtocolExceptionReference.CONNECTION_SETUP_ERROR, cause.getMessage());
    }

    public static ConnectionSetupException disconnectFailed(ConnectionException cause) {
        return new ConnectionSetupException(cause, ProtocolExceptionReference.CONNECTION_DISCONNECT_ERROR);
    }

    public static ConnectionSetupException setupOfInboundCallFailed(IOException cause) {
        return new ConnectionSetupException(cause, ProtocolExceptionReference.SETUP_OF_INBOUND_CALL_FAILED, cause.getMessage());
    }

    public static ConnectionSetupException closeOfInboundConnectorFailed(IOException cause) {
        return new ConnectionSetupException(cause, ProtocolExceptionReference.CLOSE_OF_INBOUND_CONNECTOR_FAILED, cause.getMessage());
    }
}