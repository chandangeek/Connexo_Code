package com.energyict.protocolimplv2.elster.garnet.exception;

/**
 * @author sva
 * @since 23/05/2014 - 11:21
 */
public class CrcMismatchException extends GarnetException {

    public CrcMismatchException(String s, Exception e) {
        super(s, e);
    }

    public CrcMismatchException(String s) {
        super(s);
    }

    public CrcMismatchException(Exception e) {
        super(e);
    }

    public CrcMismatchException() {
    }
}