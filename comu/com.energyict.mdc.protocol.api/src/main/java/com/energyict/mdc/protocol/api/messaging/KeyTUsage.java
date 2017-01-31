/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

public enum KeyTUsage {

    DISABLE(false, "Disabled"),
    ENABLE(true, "Enabled");

    private final boolean status;
    private final String description;

    KeyTUsage(boolean status, String description) {
        this.status = status;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Boolean fromDescription(String description) {
        for (KeyTUsage usage : values()) {
            if (usage.getDescription().equals(description)) {
                return usage.getStatus();
            }
        }
        return null;
    }

    public boolean getStatus() {
        return status;
    }

    public static String[] getAllDescriptions() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getDescription();
        }
        return result;
    }
}
