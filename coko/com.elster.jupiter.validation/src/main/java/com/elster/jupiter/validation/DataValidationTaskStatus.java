package com.elster.jupiter.validation;



public enum DataValidationTaskStatus {
    BUSY("Busy"),
    SUCCESS("Success"),
    WARNING("Warning"),
    FAILED("Failed"),
    NOT_PERFORMED("Not performed yet");

    private String name;

    DataValidationTaskStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
