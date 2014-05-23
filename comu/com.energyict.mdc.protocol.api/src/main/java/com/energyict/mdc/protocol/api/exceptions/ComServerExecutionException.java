package com.energyict.mdc.protocol.api.exceptions;

/**
 * Servers as a general exception for all exceptions which occur during the execution
 * of a DeviceProtocol, which are so severe that the framework must take notice of this.
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 11:50
 */
public class ComServerExecutionException extends RuntimeException{

    public ComServerExecutionException() {
        super();
    }

    public ComServerExecutionException(Throwable cause) {
        super(cause);
    }

    public ComServerExecutionException(String s) {
        super(s);
    }

    public ComServerExecutionException(String s, Throwable cause) {
        super(s, cause);
    }
}
