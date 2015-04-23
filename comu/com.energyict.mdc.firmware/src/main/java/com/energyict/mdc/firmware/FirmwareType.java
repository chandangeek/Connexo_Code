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
    
    public static FirmwareType get(String type) {
        for(FirmwareType t : values()) {
            if (t.getType().equals(type)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Firmware type " + type + " doesn't exist");
    }
}
