package com.energyict.protocolimplv2.dlms.idis.hs3300.registers.model;

public enum ModulationType {

    ROBO(0, "ROBO"),
    DBPSK_OR_BPSK(1, "DBPSK or BPSK"),
    DQPSK_OR_QPSK(2, "DQPSK or QPSK"),
    D8PSK_OR_8PSK(3, "D8PSK or 8PSK"),
    QUAM_16(4, "16-QAM");

    public int id;
    public String description;

    ModulationType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static String getDescription(int id) {
        for (ModulationType item : values()) {
            if (item.id == id) {
                return item.description;
            }
        }
        throw new IllegalArgumentException("Invalid ModulationType ID: " + id);
    }

}