package com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums;

public enum G3NodePhaseInfo {

    INPHASE(0, "INPHASE"),
    DEGREE60(1, "DEGREE60"),
    DEGREE120(2, "DEGREE120"),
    DEGREE180(3, "DEGREE180"),
    DEGREE240(4, "DEGREE240"),
    DEGREE300(5, "DEGREE300"),

    NOPHASEINFO(7, "NOPHASEINFO");

    private int value;
    private String description;

    G3NodePhaseInfo(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static G3NodePhaseInfo fromValue(int value) {
        for (G3NodePhaseInfo entryType : values()) {
            if (entryType.getValue() == value) {
                return entryType;
            }
        }

        return NOPHASEINFO;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

}
