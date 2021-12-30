package com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums;

public enum G3NodeState {

    UNKNOWN(0, "Unknown"),
    NOT_ASSOCIATED(1, "Not Associated"),
    AVAILABLE(2, "Available"),
    VANISHED(3, "Vanished"),
    BLACKLISTED(4, "Blacklisted");

    private int value;
    private String description;

    G3NodeState(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static G3NodeState fromValue(int value) {
        for (G3NodeState entryType : values()) {
            if (entryType.getValue() == value) {
                return entryType;
            }
        }
        throw new EnumConstantNotPresentException(G3NodeState.class,  value + " not present in G3NodeState!");
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

}
