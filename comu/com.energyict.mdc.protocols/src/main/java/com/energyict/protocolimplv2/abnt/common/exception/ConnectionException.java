package com.energyict.protocolimplv2.abnt.common.exception;


/**
 * @author sva
 * @since 23/05/2014 - 11:20
 */
public class ConnectionException extends AbntException {

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
