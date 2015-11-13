package com.energyict.protocol.exceptions;

import java.io.IOException;

/**
 * ComServer won't execute any other communication related ComCommands (for a certain connection) after that this exception is thrown.
 * It means we can no longer properly communicate with the device, because e.g. the connection to the device is lost or the logical connect failed.
 *
 * @author gna
 * @since 28/03/12 - 15:39
 */
public class ConnectionCommunicationException extends CommunicationException {

    protected ConnectionCommunicationException(Throwable cause, ProtocolExceptionReference code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected ConnectionCommunicationException(ProtocolExceptionReference reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private ConnectionCommunicationException(ProtocolExceptionReference reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }


    /**
     * Creates a new ConnectionTimeOutException
     *
     * @param totalNumberOfAttempts the total number of attempts (including the initial one) a certain request is sent, before this exception has been thrown
     */
    public static ConnectionCommunicationException connectionTimeout(final int totalNumberOfAttempts) {
        return new ConnectionCommunicationException(ProtocolExceptionReference.CONNECTION_TIMEOUT, totalNumberOfAttempts);
    }

    /**
     * An unexpected IO exception occurred while executing an operation on the input / outputstream
     * <p/>
     * After this exception, communication to the device is no longer possible.
     * The ComServer won't execute the remaining (communication related) ComCommands.
     */
    public static ConnectionCommunicationException unexpectedIOException(IOException cause) {
        return new ConnectionCommunicationException(cause, ProtocolExceptionReference.UNEXPECTED_IO_EXCEPTION, cause.getMessage());
    }

    /**
     * Creates an exception, indication that the communication failed due to the number of retries being reached.</br>
     * <i>Note: the root cause of the fail can be any IOException (e.g.: a timeout) and is given as messageArgument </i>
     * <p/>
     * After this exception, communication to the device is no longer possible.
     * The ComServer won't execute the remaining (communication related) ComCommands.
     *
     * @param totalNumberOfAttempts the total number of attempts made to transmit/receive a piece of information from the device
     * @param cause                 the exception created by the protocol
     * @return the newly created numberOfRetriesReached CommunicationException
     */
    public static ConnectionCommunicationException numberOfRetriesReached(final Exception cause, final int totalNumberOfAttempts) {
        return new ConnectionCommunicationException(ProtocolExceptionReference.NUMBER_OF_RETRIES_REACHED, cause, totalNumberOfAttempts);
    }

    /**
     * The protocol execution ran into a critical problem, the communication cannot continue.
     * E.g.: invalid framecounter, decryption failure, empty object list, ...
     */
    public static ConnectionCommunicationException unExpectedProtocolError(IOException e) {
        return new ConnectionCommunicationException(ProtocolExceptionReference.UNEXPECTED_PROTOCOL_ERROR, e);
    }

    /**
     * Creates an exception, indicating that the logical connect to a device failed.</br>
     * <i>(with logical connect we mean a signOn/logOn, the physical connection should already be established)</i>
     * <p/>
     * After this exception, communication to the device is no longer possible.
     * The ComServer won't execute the remaining (communication related) ComCommands.
     *
     * @param cause the exception created by the protocol
     * @return the newly created protocolConnect failed exception
     */
    public static ConnectionCommunicationException protocolConnectFailed(final Exception cause) {
        return new ConnectionCommunicationException(ProtocolExceptionReference.PROTOCOL_CONNECT, cause);
    }

    /**
     * Creates an exception, indication of any error related to the ciphering of data.<\br>
     * <i>(e.g.: errors popping up while encrypting data with an AES128 algorithm).</i>
     * <p/>
     * After this exception, communication to the device is no longer possible.
     * The ComServer won't execute the remaining (communication related) ComCommands.
     *
     * @param cause the exception created by the protocol
     * @return the newly created numberOfRetriesReached CommunicationException
     */
    public static ConnectionCommunicationException cipheringException(final Exception cause) {
        return new ConnectionCommunicationException(ProtocolExceptionReference.CIPHERING_EXCEPTION, cause);
    }

    /**
     * Indicates the communication was interrupted because e.g. the comserver is shutting down.
     *
     * @param cause the original InterruptedException
     */
    public static ConnectionCommunicationException communicationInterruptedException(final Exception cause) {
        return new ConnectionCommunicationException(ProtocolExceptionReference.COMMUNICATION_INTERRUPTED, cause);
    }

    /**
     * Indicates the communication was aborted by the user
     */
    public static ConnectionCommunicationException communicationAbortedByUserException() {
        return new ConnectionCommunicationException(ProtocolExceptionReference.COMMUNICATION_ABORTED_BY_USER);
    }
}