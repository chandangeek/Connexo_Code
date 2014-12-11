package com.energyict.mdc.protocol.api.messaging;

import com.energyict.mdc.protocol.api.impl.device.messages.SecurityMessage;

/**
* Copyrights EnergyICT
* Date: 12/11/14
* Time: 9:11 AM
*/
public enum SealActions {

    UNCHANGED(null, "Unchanged"),
    ENABLE_SEAL(true, "Enable seal"),
    DISABLE_SEAL(false, "Disable seal");

    private final Boolean action;
    private final String description;

    SealActions(Boolean action, String description) {
        this.action = action;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Boolean fromDescription(String description) {
        for (SealActions actions : values()) {
            if (actions.getDescription().equals(description)) {
                return actions.getAction();
            }
        }
        return null;
    }

    public Boolean getAction() {
        return action;
    }

    public static String[] getAllDescriptions() {
        String[] result = new String[values().length];
        for (int index = 0; index < values().length; index++) {
            result[index] = values()[index].getDescription();
        }
        return result;
    }
}
