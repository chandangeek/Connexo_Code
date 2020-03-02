package com.elster.jupiter.hsm.model;

public class FUAKPassiveGenerationNotSupportedException extends Exception {

    public FUAKPassiveGenerationNotSupportedException(Throwable e) {
        super(e);
    }

    public FUAKPassiveGenerationNotSupportedException(String msg) { super(msg); }

    public FUAKPassiveGenerationNotSupportedException(String s, Throwable e) { super(s, e); }
}
