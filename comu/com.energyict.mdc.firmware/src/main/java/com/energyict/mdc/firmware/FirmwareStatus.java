package com.energyict.mdc.firmware;

public enum FirmwareStatus {
    TEST("test"),
    FINAL("final"),
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