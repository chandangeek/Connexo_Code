package com.energyict.protocolimplv2.elster.garnet.exception;

/**
 * @author sva
 * @since 23/05/2014 - 11:20
 */
public class UnableToExecuteException extends GarnetException {

    public UnableToExecuteException(String s, Exception e) {
        super(s, e);
    }

    public UnableToExecuteException(String s) {
        super(s);
    }

    public UnableToExecuteException(Exception e) {
        super(e);
    }

    public UnableToExecuteException() {
    }
}
