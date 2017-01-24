package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionException;

/**
 * Wraps a connectionException into a ConnectionSetupException.
 * This exception should only be thrown when the setup of the Connection failed.
 * <p>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 10:33
 */
public class ConnectionSetupException extends CommunicationException {

    public ConnectionSetupException(MessageSeed messageSeed, ConnectionException cause) {
        super(messageSeed, cause);
    }

    public static ConnectionSetupException disconnectFailed(ConnectionException cause) {
        return new ConnectionSetupException(com.energyict.mdc.protocol.api.MessageSeeds.CONNECTION_DISCONNECT_ERROR, cause);
    }
}