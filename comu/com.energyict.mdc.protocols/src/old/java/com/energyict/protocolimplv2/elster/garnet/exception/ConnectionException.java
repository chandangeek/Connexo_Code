package com.energyict.protocolimplv2.elster.garnet.exception;

/**
 * @author sva
 * @since 23/05/2014 - 11:20
 */
public class ConnectionException extends GarnetException {

    public ConnectionException(String s, Exception e) {
        super(s, e);
    }

    public ConnectionException(String s) {
        super(s);
    }

    public ConnectionException(Exception e) {
        super(e);
    }

    public ConnectionException() {
    }
}
