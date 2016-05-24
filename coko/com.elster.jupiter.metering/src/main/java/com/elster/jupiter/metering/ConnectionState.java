package com.elster.jupiter.metering;

public enum ConnectionState {
    UNDER_CONSTRUCTION("underConstruction", "Under construction"),
    CONNECTED("connected", "Connected"),
    PHYSICALLY_DISCONNECTED("physicallyDisconnected", "Physically disconnected"),
    LOGICALLY_DISCONNECTED("logicallyDisconnected", "Logically disconnected"),
    DEMOLISHED("demolished", "Demolished");

    private String id;
    private String name;

    ConnectionState(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getName(){
        return this.name;
    }
}
