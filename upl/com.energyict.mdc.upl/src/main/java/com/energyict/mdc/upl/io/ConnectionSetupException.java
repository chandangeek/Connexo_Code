package com.energyict.mdc.upl.io;

/**
 * Models the exceptional situation that occurs when the setup of the Connection failed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-24 (15:48)
 */
public class ConnectionSetupException extends RuntimeException {
    public ConnectionSetupException() {
        super();
    }

    public ConnectionSetupException(String message) {
        super(message);
    }

    public ConnectionSetupException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionSetupException(Throwable cause) {
        super(cause);
    }

    protected ConnectionSetupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}