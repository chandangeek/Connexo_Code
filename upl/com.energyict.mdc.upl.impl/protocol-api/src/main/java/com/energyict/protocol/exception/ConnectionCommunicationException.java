/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exception;

import com.energyict.mdc.upl.nls.MessageSeed;

import com.energyict.protocol.exceptions.CommunicationInterruptedException;

import java.io.IOException;

import static com.energyict.protocol.exceptions.ConnectionCommunicationException.Type.CONNECTION_STILL_INTACT;
import static com.energyict.protocol.exceptions.ConnectionCommunicationException.Type.NON_RECOVERABLE;

/**
 * ComServer won't execute any other communication related ComCommands (for a certain connection) after that this exception is thrown.
 * It means we can no longer properly communicate with the device, because e.g. the connection to the device is lost or the logical connect failed.
 *
 * @author gna
 * @since 28/03/12 - 15:39
 */
public class ConnectionCommunicationException extends com.energyict.protocol.exceptions.ConnectionCommunicationException {

    private final Type exceptionType;

    public ConnectionCommunicationException(Type exceptionType, MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
        this.exceptionType = exceptionType;
    }

    public ConnectionCommunicationException(Type exceptionType, Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
        this.exceptionType = exceptionType;
    }

    @Override
    public Type getExceptionType() {
        return exceptionType;
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
        return new ConnectionCommunicationException(NON_RECOVERABLE, cause, ProtocolExceptionMessageSeeds.UNEXPECTED_IO_EXCEPTION, cause.getMessage());
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
    public static ConnectionCommunicationException numberOfRetriesReached(Exception cause, int totalNumberOfAttempts) {
        return new ConnectionCommunicationException(NON_RECOVERABLE, cause, ProtocolExceptionMessageSeeds.NUMBER_OF_RETRIES_REACHED, totalNumberOfAttempts);
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
    public static ConnectionCommunicationException numberOfRetriesReachedWithConnectionStillIntact(Exception cause, int totalNumberOfAttempts) {
        return new ConnectionCommunicationException(CONNECTION_STILL_INTACT, cause, ProtocolExceptionMessageSeeds.NUMBER_OF_RETRIES_REACHED_CONNECTION_STILL_INTACT, totalNumberOfAttempts);
    }

    /**
     * The protocol execution ran into a critical problem, the communication cannot continue.
     * E.g.: invalid framecounter, decryption failure, empty object list, ...
     */
    public static ConnectionCommunicationException unExpectedProtocolError(IOException e) {
        return new ConnectionCommunicationException(CONNECTION_STILL_INTACT, e, ProtocolExceptionMessageSeeds.UNEXPECTED_PROTOCOL_ERROR, e.getMessage());
    }

    /**
     * Wraps {@link com.energyict.protocol.exceptions.HsmException} to {@link ConnectionCommunicationException}.
     * @param e the {@link IOException} to wrap
     * @return {@link ConnectionCommunicationException} with appropriate message seed
     */
    public static ConnectionCommunicationException unexpectedHsmProtocolError(IOException e) {
        return new ConnectionCommunicationException(CONNECTION_STILL_INTACT, e, ProtocolExceptionMessageSeeds.COMMUNICATION_WITH_HSM, e.getMessage());
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
        return new ConnectionCommunicationException(CONNECTION_STILL_INTACT, cause, ProtocolExceptionMessageSeeds.CIPHERING_EXCEPTION, cause.getMessage());
    }

    /**
     * Error while verifying the signature of the received frame.
     * The data should have been singed by the server signing key, of which we own the certificate.
     */
    public static ConnectionCommunicationException signatureVerificationError() {
        return new ConnectionCommunicationException(NON_RECOVERABLE, ProtocolExceptionMessageSeeds.SIGNATURE_VERIFICATION_ERROR);
    }

    /**
     * Indicates the communication was interrupted because e.g. the comserver is shutting down.
     */
    public static CommunicationInterruptedException communicationInterruptedException() {
        return new CommunicationInterruptedException(ProtocolExceptionMessageSeeds.COMMUNICATION_INTERRUPTED);
    }

    /**
     * Indicates the communication was interrupted because e.g. the comserver is shutting down.
     * upl
     *
     * @param cause the original InterruptedException
     */
    public static CommunicationInterruptedException communicationInterruptedException(Exception cause) {
        return new CommunicationInterruptedException(cause, ProtocolExceptionMessageSeeds.COMMUNICATION_INTERRUPTED, cause.getMessage());
    }
}