package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.protocol.api.ConnectionException;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Wraps a {@link ConnectionException}, turning it into a Runtime exception.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since March 25, 2013 (11:30:57)
 */
public class ConnectionFailureException extends CommunicationException {

    public ConnectionFailureException (MessageSeed messageSeed, ConnectionException cause) {
        super(messageSeed, cause);
    }

}