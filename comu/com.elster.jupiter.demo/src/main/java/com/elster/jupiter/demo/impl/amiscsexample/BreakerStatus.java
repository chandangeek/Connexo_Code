package com.elster.jupiter.demo.impl.amiscsexample;

public enum BreakerStatus {
    CONNECTED("connected"),
    DISCONNECTED("disconnected"),
    ARMED("armed"),;

    private final String description;

    BreakerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
