package com.energyict.protocolimplv2.dlms.idis.hs3300.registers.model;

public enum PLCG3BandplanType {

    CENELEC_A(0, "CENELEC-A band"),
    FCC(3, "FCC band");

    private int id;
    private String description;

    PLCG3BandplanType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static String getDescription(int id) {
        for (PLCG3BandplanType item : values()) {
            if (id == item.id) {
                return item.description;
            }
        }
        throw new IllegalArgumentException("Invalid PLC G3 Bandplan ID: " + id);
    }

}
