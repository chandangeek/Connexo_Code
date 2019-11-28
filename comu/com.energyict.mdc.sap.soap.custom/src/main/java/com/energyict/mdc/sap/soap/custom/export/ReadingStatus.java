/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.export;

public enum ReadingStatus {
    ACTUAL("ACTL"),
    INVALID("INVL");

    private String value;

    ReadingStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
