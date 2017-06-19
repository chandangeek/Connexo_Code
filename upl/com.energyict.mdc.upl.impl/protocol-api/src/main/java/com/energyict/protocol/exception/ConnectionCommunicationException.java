/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exception;

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
     * An unexpected IO exception occurred while executing an operation on the input / outputstream.
     * This means that the underlying connection is broken and can no longer be used!
     * <p>
     * After this exception, communication to the device is no longer possible.
     * The ComServer won't execute the remaining (communication related) ComCommands.
     * Also, communication to the next physical slave devices (that use the same connection) is not possible.
     */
    public static ConnectionCommunicationException unexpectedIOException(IOException cause) {
        return new ConnectionCommunicationException(cause, ProtocolExceptionReference.UNEXPECTED_IO_EXCEPTION, cause.getMessage());
    }

    /**
     * Creates an exception, indication that the communication failed due to the number of retries being reached.</br>
     * <i>Note: the root cause of the fail can be any IOException (e.g.: a timeout) and is given as messageArgument </i>
     * <p>
     * After this exception, communication to the device is no longer possible.
     * The ComServer won't execute the remaining (communication related) ComCommands.
     *
     * @param totalNumberOfAttempts the total number of attempts made to transmit/receive a piece of information from the device
     * @param cause the exception created by the protocol
     * @return the newly created numberOfRetriesReached CommunicationException
     */
    public static ConnectionCommunicationException numberOfRetriesReached(final Exception cause, final int totalNumberOfAttempts) {
        return new ConnectionCommunicationException(ProtocolExceptionReference.NUMBER_OF_RETRIES_REACHED, cause, totalNumberOfAttempts);
    }

    /**
     * Creates an exception, indication that the communication failed due to the number of retries being reached.</br>
     * <i>Note: the root cause of the fail can be any IOException (e.g.: a timeout) and is given as messageArgument </i>
     * <p>
     * After this exception, communication to the device is no longer possible. The ComServer won't execute the remaining (communication related) ComCommands.
     * However, the connection is still intact, so the next physical slave devices (that use this same connection) can still be read out in this case
     *
     * @param totalNumberOfAttempts the total number of attempts made to transmit/receive a piece of information from the device
     * @param cause the exception created by the protocol
     * @return the newly created numberOfRetriesReached CommunicationException
     */
    public static ConnectionCommunicationException numberOfRetriesReachedWithConnectionStillIntact(final Exception cause, final int totalNumberOfAttempts) {
        return new ConnectionCommunicationException(ProtocolExceptionReference.NUMBER_OF_RETRIES_REACHED_CONNECTION_STILL_INTACT, cause, totalNumberOfAttempts);
    }

    /**
     * The protocol execution ran into a critical problem, the communication cannot continue.
     * E.g.: invalid framecounter, decryption failure, empty object list, ...
     */
    public static ConnectionCommunicationException unExpectedProtocolError(IOException e) {
        return new ConnectionCommunicationException(ProtocolExceptionReference.UNEXPECTED_PROTOCOL_ERROR, e);
    }

    /**
     * Creates an exception, indication of any error related to the ciphering of data.<\br>
     * <i>(e.g.: errors popping up while encrypting data with an AES128 algorithm).</i>
     * <p>
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
     * Error while verifying the signature of the received frame.
     * The data should have been singed by the server signing key, of which we own the certificate.
     */
    public static ConnectionCommunicationException signatureVerificationError() {
        return new ConnectionCommunicationException(ProtocolExceptionReference.SIGNATURE_VERIFICATION_ERROR);
    }

    /**
     * Indicates the communication was interrupted because e.g. the comserver is shutting down.
     */
    public static ConnectionCommunicationException communicationInterruptedException() {
        return new ConnectionCommunicationException(ProtocolExceptionReference.COMMUNICATION_INTERRUPTED);
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