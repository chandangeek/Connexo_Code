package com.energyict.mdc.firmware;

public enum FirmwareStatus {
    TEST("test"),
    FINAL("final"),
    GHOST("ghost"),
    DEPRECATED("deprecated");

    private String status;

    private FirmwareStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static FirmwareStatus from(String status) {
        switch (status.toLowerCase()) {
            case "test":
                return FirmwareStatus.TEST;
            case "final":
                return FirmwareStatus.FINAL;
            case "ghost":
                return FirmwareStatus.GHOST;
            case "deprecated":
                return FirmwareStatus.DEPRECATED;
            default:
                throw new IllegalArgumentException("Firmware status " + status + " doesn't exist");
        }
    }
}
