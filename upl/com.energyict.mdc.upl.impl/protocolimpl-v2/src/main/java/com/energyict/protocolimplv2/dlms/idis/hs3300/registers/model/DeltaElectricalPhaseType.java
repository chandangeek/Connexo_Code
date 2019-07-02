package com.energyict.protocolimplv2.dlms.idis.hs3300.registers.model;

public enum DeltaElectricalPhaseType {

    NOT_DEFINED(0, "Not defined"),
    DEGREES_0(1, "0 degree"),
    DEGREES_60(2, "60 degree"),
    DEGREES_120(3, "120 degree"),
    DEGREES_180(4, "180 degree"),
    DEGREES_MIN_120(5, "-120 degree"),
    DEGREES_MIN_60(6, "-60 degree");

    private int id;
    private String description;

    DeltaElectricalPhaseType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static String getDescription(int id) {
        for (DeltaElectricalPhaseType item : values()) {
            if (id == item.id) {
                return item.description;
            }
        }
        throw new IllegalArgumentException("Invalid Delta Electrical Phase ID: " + id);
    }

}