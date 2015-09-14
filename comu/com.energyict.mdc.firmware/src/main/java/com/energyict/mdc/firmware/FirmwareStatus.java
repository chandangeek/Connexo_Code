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

    public static FirmwareStatus from(String status) {
        for(FirmwareStatus s: values()) {
            if (s.getStatus().equals(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Firmware status " + status + " doesn't exist");
    }

}