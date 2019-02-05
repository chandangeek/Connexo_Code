/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

public class ValueParserException extends Exception {

    private String rawValue;

    public ValueParserException(String rawValue) {
        this.rawValue = rawValue;
    }

    public String getValue() {
        return rawValue;
    }
}
