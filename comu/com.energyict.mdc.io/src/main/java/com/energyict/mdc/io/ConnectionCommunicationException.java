package com.energyict.mdc.io;

import com.energyict.mdc.io.impl.MessageSeeds;

import com.elster.jupiter.util.exception.MessageSeed;

import java.io.IOException;

/**
 * Models the exceptional situation that occurs when underlying
 * communication mechanisms report an IOException that is so severe
 * that the execution of any other ComCommand will fail.
 * In other words, the current connection with the physical device
 * can no longer be used to communicate with the device.
 * Examples of such severe errors:
 * <ul>
 * <li>physical connection with the device is lost</li>
 * <li>logical connect failed</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-24 (15:40)
 */
public class ConnectionCommunicationException extends CommunicationException {

    /**
     * Creates a new ConnectionCommunicationException.
     *
     * @param totalNumberOfAttempts the total number of attempts (including the initial one) a certain request is sent, before this exception has been thrown
     */
    public ConnectionCommunicationException(int totalNumberOfAttempts) {
        super(MessageSeeds.CONNECTION_TIMEOUT, totalNumberOfAttempts);
    }

    /**
     * An unexpected IO exception occurred while executing an operation on the input / outputstream
     * <p/>
     * After this exception, communication to the device is no longer possible.
     * The ComServer won't execute the remaining (communication related) ComCommands.
     */
    public ConnectionCommunicationException(MessageSeed messageSeed, IOException cause) {
        super(messageSeed, cause);
    }

    /**
     * Indicates the communication was interrupted maybe because e.g. the comserver is shutting down.
     * <br>
     * After this exception, communication to the device is no longer possible.
     * The ComServer won't execute the remaining (communication related) ComCommands.
     */
    public ConnectionCommunicationException(InterruptedException cause) {
        super(MessageSeeds.COMMUNICATION_INTERRUPTED, cause);
    }

    /**
     * An communication exception occurred while executing an operation on the input / outputstream
     * <p/>
     * After this exception, communication to the device is no longer possible.
     * The ComServer won't execute the remaining (communication related) ComCommands.
     */
    public ConnectionCommunicationException(MessageSeed messageSeed, CommunicationException cause) {
        super(messageSeed, cause);
    }

}