package com.energyict.mdc.io;

import java.io.IOException;

/**
 * NestedIOException represents an IOException caused by an other Exception.
 */
public class NestedIOException extends IOException {

    /**
     * Creates a new instance.
     *
     * @param cause the exception that causes the NestedIOException
     */
    public NestedIOException(Throwable cause) {
        super(cause.toString());
        initCause(cause);
    }

    /**
     * Creates a new instance.
     *
     * @param cause the exception that causes the NestedIOException
     * @param str   description about interrupt
     */
    public NestedIOException(Throwable cause, String str) {
        super(cause.toString() + ", " + str);
        initCause(cause);
    }

}