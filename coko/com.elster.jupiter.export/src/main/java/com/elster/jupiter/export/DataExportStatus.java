package com.elster.jupiter.export;

public enum DataExportStatus {
    BUSY("Busy"),
    SUCCESS("Success"),
    WARNING("Warning"),
    FAILED("Failed"),
    NOT_PERFORMED("Not performed yet");

    private String name;

    DataExportStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
