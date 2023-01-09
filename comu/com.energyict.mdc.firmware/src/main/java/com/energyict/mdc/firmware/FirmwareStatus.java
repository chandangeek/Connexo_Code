/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

public enum FirmwareStatus {
    // The order defines the order of statuses & related firmware check options in UI
    TEST("test"),
    FINAL("final"),
    GHOST("ghost"),
    DEPRECATED("deprecated");

    private final String status;

    FirmwareStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
