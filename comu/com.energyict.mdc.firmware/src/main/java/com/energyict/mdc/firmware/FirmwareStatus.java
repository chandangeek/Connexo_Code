package com.energyict.mdc.firmware;

public enum FirmwareStatus {
    TEST("test", "Test"),
    FINAL("final", "Final"),
    GHOST("ghost", "Ghost"),
    DEPRECATED("deprecated", "Deprecated");

    private String status;
    private String displayValue;

    private FirmwareStatus(String status, String displayValue) {
        this.status = status;
        this.displayValue = displayValue;
    }

    public String getStatus() {
        return status;
    }

    public String getDisplayValue() {
        return displayValue;
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
