package com.energyict.mdc.firmware;

public enum FirmwareType {
    COMMUNICATION("communication"),
    METER("meter");

    private String type;

    private FirmwareType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static FirmwareType from(String type) {
        switch(type.toLowerCase()) {
            case "communication":
                return FirmwareType.COMMUNICATION;
            case "meter":
                return FirmwareType.METER;
            default:
                throw new IllegalArgumentException("Firmware type " + type + " doesn't exist");
        }
    }
}
