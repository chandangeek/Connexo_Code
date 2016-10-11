package com.elster.jupiter.metering;

import com.elster.jupiter.nls.TranslationKey;

public enum ConnectionState implements TranslationKey {
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

    @Override
    public String getKey() {
        return "connection.state." + this.id;
    }

    @Override
    public String getDefaultFormat() {
        return this.name;
    }

}
