package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/04/2016 - 14:22
 */
public enum MulticastUpgradeState {

    NotStarted(0, "The multicast upgrade has not started"),
    Pending(1, "The upgrade is waiting to be executed"),
    Running(2, "The upgrade is in progress"),
    Failed(3, "The upgrade failed"),
    Finished(4, "The upgrade is finished"),
    Unknown(5, "The upgrade is in an unknown execution state"), ;

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
        return Unknown;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}