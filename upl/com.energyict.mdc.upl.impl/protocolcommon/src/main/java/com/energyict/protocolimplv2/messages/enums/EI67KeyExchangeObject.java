/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.enums;


public enum EI67KeyExchangeObject {

    MANAGEMENT(1, "0.0.40.0.1.255"),
    INSTALLER(2, "0.0.40.0.3.255");

    private final int attributeNumber;
    private final String obis;

    EI67KeyExchangeObject(int attributeNumber, String obis) {
        this.attributeNumber = attributeNumber;
        this.obis = obis;
    }

    public static int getAttributeNumberByObject(String description) {
        for (EI67KeyExchangeObject roles : values()) {
            if (roles.getObis().equals(description)) {
                return roles.getAttributeNumber();
            }
        }
        return -1;
    }

    public static String[] getAllObjects() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getObis();
        }
        return result;
    }

    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    public String getObis() {
        return this.obis;
    }
}
