/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

public enum FirmwareStatus {
    // The order defines how firmware check options are displayed in UI
    FINAL("final"),
    TEST("test"),
    GHOST("ghost"),
    DEPRECATED("deprecated");

    private String status;

    FirmwareStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
