/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

public enum ConnectionStatusProcessingResultCode {
    SUCCESSFUL("1"),
    FAILED("2");

    private final String code;

    ConnectionStatusProcessingResultCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
