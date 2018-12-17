/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

public enum ProcessingResultCode {

    SUCCESSFUL("3"),
    FAILED("5"),
    ;

    private final String code;

    ProcessingResultCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}