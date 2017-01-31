/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

public enum CipheringType {

    GLOBAL(0, "Global ciphering"),
    DEDICATED(1, "Dedicated ciphering"),
    GENERAL_GLOBAL(2, "General-global ciphering"),
    GENERAL_DEDICATED(3, "General-dedicated ciphering"),
    GENERAL_CIPHERING(4, "General ciphering"),
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
