package com.energyict.mdc.upl.io;

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
 * @since 2017-01-24 (12:44)
 */
public class ConnectionCommunicationException extends RuntimeException {

    private final Type type;
    private final Object[] messageArguments;

    public enum Type {
        /**
         * Wraps an {@link IOException} that was not expected.
         * No additional arguments.
         */
        UNEXPECTED_IO_EXCEPTION,

        /**
         * Wraps an {@link InterruptedException} that was produced by the system
         * likely in the time that the protocol was waiting on IO.
         * Note that the actual InterruptedException is not always available.
         * No additional arguments.
         */
        INTERRUPTED_BY_SYSTEM,

        /**
         * Wraps the exception that caused the last in a series of attempts to fail.
         * As that was the last attempt before the maximum number of allowed attempts
         * was exceeded, the communication was interrupted.
         * The exception that caused the last attempt to fail is available.
         * The total number of attempts is available as argument.
         */
        INTERRUPTED_BY_EXCEEDED_ALLOWED_NUMBER_OF_ATTEMPTS,

        /**
         * Indicates that the protocol was interrupted by the user.
         * No additional arguments.
         */
        INTERRUPTED_BY_USER;
    }

    public static ConnectionCommunicationException unexpectedIOException(IOException unexpected) {
        return new ConnectionCommunicationException(Type.UNEXPECTED_IO_EXCEPTION, unexpected);
    }

    public static ConnectionCommunicationException systemInterrupted() {
        return new ConnectionCommunicationException(Type.INTERRUPTED_BY_SYSTEM);
    }

    public static ConnectionCommunicationException systemInterrupted(InterruptedException e) {
        return new ConnectionCommunicationException(Type.INTERRUPTED_BY_SYSTEM, e);
    }

    public static ConnectionCommunicationException allowedAttemptsExceeded(Exception e, int totalNumberOfAttempts) {
        return new ConnectionCommunicationException(Type.INTERRUPTED_BY_EXCEEDED_ALLOWED_NUMBER_OF_ATTEMPTS, e, totalNumberOfAttempts);
    }

    public static ConnectionCommunicationException userInterrupted() {
        return new ConnectionCommunicationException(Type.INTERRUPTED_BY_USER);
    }

    public Type getType() {
        return type;
    }

    public Object[] getMessageArguments() {
        return messageArguments;
    }

    private ConnectionCommunicationException(Type type, Object... messageArguments) {
        this.type = type;
        this.messageArguments = messageArguments;
    }

    private ConnectionCommunicationException(Type type, Throwable t, Object... messageArguments) {
        super(t);
        this.type = type;
        this.messageArguments = messageArguments;
    }

}