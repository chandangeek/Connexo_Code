package com.energyict.protocolimplv2.messages.enums;

/**
 * Copyrights EnergyICT
 * Date: 25/10/13
 * Time: 17:27
 * Author: khe
 */
public enum ActivityCalendarType {

    PublicNetwork("PublicNetwork"),
    Provider("Provider");

    private final String description;

    ActivityCalendarType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static String[] getAllDescriptions() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getDescription();
        }
        return result;
    }
}