package com.energyict.mdc.device.data.importers.impl.exceptions;

public abstract class ImportException extends RuntimeException {
    String expected;
    String actual;

    public abstract String getMessage();

    public String getMessageParameter() {
        return expected.isEmpty() ? null :
                actual.isEmpty() ? expected : expected + ";" + actual;
    }
}