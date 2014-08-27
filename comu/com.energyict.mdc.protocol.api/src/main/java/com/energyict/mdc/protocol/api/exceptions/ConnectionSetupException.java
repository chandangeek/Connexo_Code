package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.protocol.api.ConnectionException;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Wraps a connectionException into a ConnectionSetupException.
 * This exception should only be thrown when the setup of the Connection failed.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 10:33
 */
public class ConnectionSetupException extends CommunicationException {

    public ConnectionSetupException(MessageSeed messageSeed, ConnectionException cause) {
        super(messageSeed, cause);
    }

}