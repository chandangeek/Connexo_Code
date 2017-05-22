/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exception;

import com.energyict.protocol.exceptions.ConnectionException;

import java.io.IOException;

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

    public static ConnectionSetupException configurationException(RuntimeException cause) {
        return new ConnectionSetupException(cause, ProtocolExceptionReference.CONNECTION_SETUP_ERROR, cause.getMessage());
    }
}