package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum ConnectionStateTranslationKeys implements TranslationKey {
    UNDER_CONSTRUCTION(ConnectionState.UNDER_CONSTRUCTION, "Under construction"),
    CONNECTED(ConnectionState.CONNECTED, "Connected"),
    PHYSICALLY_DISCONNECTED(ConnectionState.PHYSICALLY_DISCONNECTED, "Physically disconnected"),
    LOGICALLY_DISCONNECTED(ConnectionState.LOGICALLY_DISCONNECTED, "Logically disconnected"),
    DEMOLISHED(ConnectionState.DEMOLISHED, "Demolished");

    private final ConnectionState connectionState;
    private final String defaultFormat;

    ConnectionStateTranslationKeys(ConnectionState connectionState, String defaultFormat) {
        this.connectionState = connectionState;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return "connection.state." + this.connectionState.getId();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static ConnectionStateTranslationKeys getTranslatedKeys(ConnectionState connectionState) {
        return Arrays.stream(ConnectionStateTranslationKeys.values())
                .filter(key -> key.connectionState == connectionState)
                .findFirst()
                .orElse(UNDER_CONSTRUCTION);
    }
}
