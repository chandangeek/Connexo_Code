package com.energyict.protocolimplv2.elster.garnet.exception;

import java.io.IOException;

/**
 * @author sva
 * @since 23/05/2014 - 10:14
 */
public class GarnetException extends IOException {

    public GarnetException() {
        super();
    }

    public GarnetException(String s) {
        super(s);
    }

    public GarnetException(String s, Exception e) {
        super(s + ": " + e.getMessage());
        initCause(e);
    }

    public GarnetException(Exception e) {
        initCause(e);
    }
}
