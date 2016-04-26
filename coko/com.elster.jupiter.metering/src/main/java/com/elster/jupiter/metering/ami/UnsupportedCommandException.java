package com.elster.jupiter.metering.ami;

public class UnsupportedCommandException extends Exception {

    public UnsupportedCommandException() {
    }

    public UnsupportedCommandException(String msg) {
        super(msg);
    }

}
