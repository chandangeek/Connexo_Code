package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/04/2016 - 14:22
 */
public enum MulticastUpgradeState {

    INITIAL(0, "The multicast upgrade has not started"),
    IMAGE_PRESENT(1, "A valid slave image is present"),
    PROTOCOL_CONFIG_PRESENT(2, "A valid protocol configuration is present"),
    IMAGE_AND_PROTOCOL_CONFIG_PRESENT(3, "A valid slave image and protocol configuration are present"),
    PENDING(4, "The upgrade process was requested and waiting to be executed"),
    RUNNING(5, "The upgrade is in progress"),
    FAILED(6, "The upgrade failed"),
    FINISHED(7, "The upgrade has finished"),
    UNKNOWN(-1, "The state of the upgrade is unknown.");

    private int value;
    private String description;

    MulticastUpgradeState(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static MulticastUpgradeState fromValue(int value) {
        for (MulticastUpgradeState multicastUpgradeState : values()) {
            if (multicastUpgradeState.getValue() == value) {
                return multicastUpgradeState;
            }
        }
        return UNKNOWN;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}