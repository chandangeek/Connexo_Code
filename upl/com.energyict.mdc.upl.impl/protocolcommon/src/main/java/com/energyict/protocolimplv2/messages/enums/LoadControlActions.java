package com.energyict.protocolimplv2.messages.enums;

/**
 * Copyrights EnergyICT
 * Date: 25/10/13
 * Time: 17:27
 * Author: khe
 */
public enum LoadControlActions {

    Nothing(0, "Nothing"),
    Reconnect(2, "Reconnect"),
    Invalid(-1, "Invalid");

    private final String description;
    private final int id;

    LoadControlActions(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static LoadControlActions fromDescription(String description) {
        for (LoadControlActions actions : values()) {
            if (actions.description.equals(description)) {
                return actions;
            }
        }
        return Invalid;
    }

    public int getId() {
        return id;
    }

    public static String[] getAllDescriptions() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getDescription();
        }
        return result;
    }
}