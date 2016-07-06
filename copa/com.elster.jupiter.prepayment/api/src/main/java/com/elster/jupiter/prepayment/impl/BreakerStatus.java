package com.elster.jupiter.prepayment.impl;

public enum BreakerStatus {
    connected("connected"),
    disconnected("disconnected"),
    armed("armed"),;

    private final String description;

    BreakerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
