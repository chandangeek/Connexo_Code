/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp.registers;

public enum RelayOperatingMode {
    IP_SWITCH_OFF(0, "IP switch off - permanent state"),
    IP_SWITCH_ON(1, "IP switch on - permanent state"),
    ASTRONOMICAL_CLOCK(2, "Astronomical clock (default mode)"),
    TIME_SWITCHING_TABLE(3, "Time switching table"),
    UNKNOWN(-1, "Unknown");

    private final String description;
    private final int value;

    RelayOperatingMode(int value, String description) {
        this.description = description;
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return value;
    }

    public static RelayOperatingMode fromValue(int value) {
        for (RelayOperatingMode relayOperatingMode : values()) {
            if (relayOperatingMode.getValue() == value) {
                return relayOperatingMode;
            }
        }
        return UNKNOWN;
    }
}