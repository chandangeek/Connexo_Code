package com.energyict.mdc.protocol.api;

/**
 * This exception is thrown when an optional operation is not support by the
 * specific protocol.
 *
 * @author Karel
 */
public class UnsupportedException extends ProtocolException {

    public UnsupportedException() {
    }

    public UnsupportedException(String msg) {
        super(msg);
    }

}