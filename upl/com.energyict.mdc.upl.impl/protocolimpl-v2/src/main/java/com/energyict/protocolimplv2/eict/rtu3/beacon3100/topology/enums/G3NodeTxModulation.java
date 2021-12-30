package com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums;

public enum G3NodeTxModulation {

    ROBO(0, "ROBO"),
    DBPSK(1, "DBPSK"),
    DQPSK(2, "DQPSK"),
    D8PSK(3, "D8PSK"),
    QAM16(4, "QAM16"),
    SUPERROBO(5, "SUPERROBO"),
    UNKNOWN(99, "UNKNOWN");

    private int value;
    private String description;

    G3NodeTxModulation(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static G3NodeTxModulation fromValue(int value) {
        for (G3NodeTxModulation entryType : values()) {
            if (entryType.getValue() == value) {
                return entryType;
            }
        }

        return UNKNOWN;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public boolean isKnown() {
        return value!= UNKNOWN.value;
    }
}
