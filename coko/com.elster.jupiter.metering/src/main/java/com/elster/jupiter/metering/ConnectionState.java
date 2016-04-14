package com.elster.jupiter.metering;

public enum ConnectionState {
    UNDER_CONSTRUCTION("underConstruction"),
    CONNECTED("connected"),
    PHYSICALLY_DISCONNECTED("physicallyDisconnected"),
    LOGICALLY_DISCONNECTED("logicallyDisconnected"),
    DEMOLISHED("demolished");

    private String id;

    ConnectionState(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
