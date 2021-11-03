package com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums;

public enum G3NodeNodeModulationScheme {

    DIFFERENTIAL(0, "DIFFERENTIAL"),
    COHERENT(1, "COHERENT"),
    UNKNOWN(255, "UNKNOWN");

    private int value;
    private String description;

    G3NodeNodeModulationScheme(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static G3NodeNodeModulationScheme fromValue(int value) {
        for (G3NodeNodeModulationScheme entryType : values()) {
            if (entryType.getValue() == value) {
                return entryType;
            }
        }
        return UNKNOWN;
    }

    public static G3NodeNodeModulationScheme fromValue(boolean payloadModulationScheme) {
        return payloadModulationScheme?COHERENT:DIFFERENTIAL;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

}
