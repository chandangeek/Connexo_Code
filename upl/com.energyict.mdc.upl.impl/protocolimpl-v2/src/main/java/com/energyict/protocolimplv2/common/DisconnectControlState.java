package com.energyict.protocolimplv2.common;

/**
 * Copyrights EnergyICT
 * Date: 11/02/14
 * Time: 11:23
 * Author: khe
 */
public enum DisconnectControlState {

    DISCONNECTED(0, "Disconnected"),
    CONNECTED(1, "Connected"),
    READY_FOR_RECONNECT(2, "Ready for reconnection"),
    UNKNOWN(-1, "Unknown state");

    private final String description;
    private final int value;

    DisconnectControlState(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static DisconnectControlState fromValue(int value) {
        for (DisconnectControlState state : values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        return UNKNOWN;
    }
}
