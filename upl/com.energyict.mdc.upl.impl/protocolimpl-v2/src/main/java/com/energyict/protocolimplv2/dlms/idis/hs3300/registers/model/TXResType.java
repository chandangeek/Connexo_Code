package com.energyict.protocolimplv2.dlms.idis.hs3300.registers.model;

public enum TXResType {

    SIX_DB(0, "6 dB"),
    THREE_DB(1, "3 dB");

    public int id;
    public String description;

    TXResType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static String getDescription(int id) {
        for (TXResType item : values()) {
            if (item.id == id) {
                return item.description;
            }
        }
        throw new IllegalArgumentException("Invalid TXResType ID: " + id);
    }

}