package com.energyict.protocol.exceptions;

import java.text.MessageFormat;

/**
 * Models the exceptional situation that occurs when a connection
 * with a device could not be established.
 * There will always be a nested exception that
 * provides details of the failure.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (12:10)
 */
public class ConnectionException extends Exception {


    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionException(String pattern, Object... arguments) {
        super(format(pattern, arguments));
    }

    public ConnectionException(String pattern, Throwable ex, Object... arguments) {
        super(format(pattern, arguments), ex);
    }

    public ConnectionException(Throwable cause) {
        super((cause == null ? null : cause.getMessage()), cause);
    }

    private static String format(String pattern, Object[] arguments) {
        return MessageFormat.format(pattern.replaceAll("'", "''"), arguments);
    }
}