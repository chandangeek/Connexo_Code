/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.exceptions;

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
