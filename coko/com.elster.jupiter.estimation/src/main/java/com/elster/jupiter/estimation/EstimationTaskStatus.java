package com.elster.jupiter.estimation;

public enum EstimationTaskStatus {
    BUSY("Busy"),
    SUCCESS("Success"),
    WARNING("Warning"),
    FAILED("Failed"),
    NOT_PERFORMED("Not performed yet");

    private String name;

    EstimationTaskStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
