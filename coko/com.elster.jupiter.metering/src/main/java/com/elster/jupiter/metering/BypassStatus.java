package com.elster.jupiter.metering;

public enum BypassStatus {
    OPEN("Open"),
    CLOSED("Closed"),
    UNKNOWN("Unknown");

    private String displayValue;

    BypassStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
