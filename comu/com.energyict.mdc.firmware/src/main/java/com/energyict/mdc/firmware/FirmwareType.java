package com.energyict.mdc.firmware;

public enum FirmwareType {
    COMMUNICATION("communication"),
    METER("meter");

    private String type;

    FirmwareType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}