/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

public enum SeverityCode {

    INFORMATION("1"),
    WARNING("2"),
    ERROR("3"),
    ABORT("4"),
    ;

    private final String code;

    SeverityCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
