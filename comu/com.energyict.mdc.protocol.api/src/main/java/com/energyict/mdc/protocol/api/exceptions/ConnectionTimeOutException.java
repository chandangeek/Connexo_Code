package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;

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
    public ConnectionTimeOutException(MessageSeed messageSeed, int totalNumberOfAttempts) {
        super(messageSeed, totalNumberOfAttempts);
    }

}