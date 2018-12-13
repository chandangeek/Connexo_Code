package com.elster.jupiter.hsm.model;

public class HsmBaseException extends Exception {
    public HsmBaseException(Throwable e) {
        super(e);
    }

    public HsmBaseException(String msg) { super(msg);  }

    public HsmBaseException(String s, Throwable e) { super(s, e);
    }
}
