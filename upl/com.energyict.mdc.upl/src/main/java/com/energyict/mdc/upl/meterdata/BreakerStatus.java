package com.energyict.mdc.upl.meterdata;

/**
 * Enum listing up all possible statuses of the device breaker
 *
 * @author sva
 * @since 7/04/2016 - 10:10
 */
public enum BreakerStatus {
    DISCONNECTED(0, "Disconnected"),
    CONNECTED(1, "Connected"),
    ARMED(2, "Ready for reconnection"),
    UNKNOWN(-1, "Unknown state");

    private final String description;
    private final int value;

    BreakerStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static BreakerStatus fromValue(int value) {
        for (BreakerStatus state : values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        return UNKNOWN;
    }

    public static BreakerStatus fromDescription(String descr) {
        for (BreakerStatus state : values()) {
            if (state.getDescription() == descr) {
                return state;
            }
        }
        return UNKNOWN;
    }
}