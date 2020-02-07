/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import java.util.stream.Stream;

public enum ProcessingResultCode {
    RECEIVED("1"),
    IN_PROCESS("2"),
    SUCCESSFUL("3"),
    PARTIALLY_SUCCESSFUL("4"),
    FAILED("5");

    private final String code;

    ProcessingResultCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ProcessingResultCode valueFor(String code) {
        return Stream
                .of(values())
                .filter(each -> each.getCode().equals(code))
                .findAny()
                .orElse(null);
    }
}
