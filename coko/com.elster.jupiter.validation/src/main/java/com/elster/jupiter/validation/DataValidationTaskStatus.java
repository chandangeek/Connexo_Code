package com.elster.jupiter.validation;



public enum DataValidationTaskStatus {
    BUSY("Ongoing"),
    SUCCESS("Successful"),
    WARNING("Warning"),
    FAILED("Failed"),
    NOT_PERFORMED("Created");

    private String name;

    DataValidationTaskStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
