package com.energyict.dlms;

/**
 * Copyrights EnergyICT
 * Date: 11/02/11
 * Time: 14:38
 */
public enum CipheringType {

    GLOBAL(0, "Global ciphering"),
    DEDICATED(1, "Dedicated ciphering"),
    INVALID(-1, "Invalid");

    private int type;
    private String description;

    private CipheringType(int type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public String getTypeString() {
        return "" + getType();
    }

    public static CipheringType fromValue(int type) {
        for (CipheringType cipheringType : values()) {
            if (cipheringType.getType() == type) {
                return cipheringType;
            }
        }
        return INVALID;
    }

}
