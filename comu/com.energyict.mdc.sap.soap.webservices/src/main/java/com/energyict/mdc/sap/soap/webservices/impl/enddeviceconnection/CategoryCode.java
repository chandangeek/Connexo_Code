/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import java.util.Arrays;

public enum CategoryCode {

    DISCONNECT("1"),
    CONNECT("2"),
    UNKNOWN("99")

    ;

    private final String code;

    CategoryCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CategoryCode findByCode(String code) {
        return Arrays.stream(CategoryCode.values())
                .filter(value -> value.getCode().equals(code))
                .findFirst()
                .orElse(UNKNOWN);
    }
}