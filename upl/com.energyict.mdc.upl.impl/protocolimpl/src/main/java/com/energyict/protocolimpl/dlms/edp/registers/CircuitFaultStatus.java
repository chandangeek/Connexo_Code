package com.energyict.protocolimpl.dlms.edp.registers;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 9:34
 * Author: khe
 */
public enum CircuitFaultStatus {

    NO_FAULT(0, "No fault"),
    NO_CONSUMPTION(1, "No consumption in IP circuit with \"IP control - current status\" connected."),
    CONSUMPTION_HIGHER(2, "Consumption higher than the configured threshold with \"IP control - current status\" disconnected."),
    CONSUMPTION_OVER_THRESHOLD(3, "Consumption in IP circuit over the configured maximum threshold with \"IP control - current\" status connected."),
    CONSUMPTION_UNDER_THRESHOLD(4, "Consumption in IP circuit under the configured minimum threshold with \"IP control - current\" status connected."),
    UNKNOWN(-1, "Unknown status");

    private final String description;
    private final long status;

    CircuitFaultStatus(long status, String description) {
        this.status = status;
        this.description = description;
        }

    public String getDescription() {
        return description;
        }

    public long getStatus() {
        return status;
        }

    public static CircuitFaultStatus fromValue(long value) {
        for (CircuitFaultStatus circuitFaultStatus : values()) {
            if (value == circuitFaultStatus.getStatus()) {
                return circuitFaultStatus;
            }
        }
        return UNKNOWN;
    }
}