package com.elster.jupiter.metering.imports.impl.usagepoint.exceptions;

public class ValueParserException extends RuntimeException {

    private String rawValue;
    private String expected;

    public ValueParserException(String rawValue, String expected) {
        this.rawValue = rawValue;
        this.expected = expected;
    }

    public String getValue() {
        return rawValue;
    }

    public String getExpected() {
        return expected;
    }
}
