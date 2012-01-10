package com.energyict.protocolimpl.base;

import java.io.IOException;

/**
 * This exception can be used if we catch an exception and want to throw an IOException, but want to preserve the original cause.
 * In this case, we just have to replace the original IOException with this class, and pass the first exception as cause.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/01/12
 * Time: 8:40
 */
public class IOExceptionWithCause extends IOException {

    /**
     * Create a new IOExceptionWithCause with a message and a cause
     *
     * @param message The detailed message of the IOException
     * @param cause   The previous exception to preserve
     */
    public IOExceptionWithCause(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    /**
     * Create a new IOExceptionWithCause with given a cause.
     *
     * @param cause The previous exception to preserve
     */
    public IOExceptionWithCause(Throwable cause) {
        super(cause == null ? null : cause.getMessage());
        initCause(cause);
    }

}
