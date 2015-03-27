package com.energyict.mdc.firmware;

public enum FirmwareType {
    COMMUNICATION("communication", "Communication firmware"),
    METER("meter", "Meter firmware");

    private String type;
    private String displayValue;

    private FirmwareType(String type, String displayValue) {
        this.type = type;
        this.displayValue = displayValue;
    }

    public String getType() {
        return type;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return getType();
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
