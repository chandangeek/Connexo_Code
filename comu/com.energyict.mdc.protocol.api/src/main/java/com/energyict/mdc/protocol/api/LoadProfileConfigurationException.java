package com.energyict.mdc.protocol.api;

/**
 * Exception can be thrown when the configuration of a loadProfile does not match with the configuration in the meter
 */
public class LoadProfileConfigurationException extends ProtocolException {

    /**
     * Constructs an <code>ProtocolException</code> with <code>null</code>
     * as its error detail message.
     */
    public LoadProfileConfigurationException() {
        super();
    }

    /**
     * Constructs an <code>ProtocolException</code> with the specified detail
     * message. The error message string <code>s</code> can later be
     * retrieved by the <code>{@link java.lang.Throwable#getMessage}</code>
     * method of class <code>java.lang.Throwable</code>.
     *
     * @param msg the detail message.
     */
    public LoadProfileConfigurationException(String msg) {
        super(msg);
    }
}
