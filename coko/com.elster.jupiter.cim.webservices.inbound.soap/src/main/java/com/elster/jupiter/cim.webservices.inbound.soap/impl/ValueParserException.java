/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

public class ValueParserException extends Exception {

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
