/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.bpm.impl;

import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    USAGE_POINT_ASSOCIATION_PROVIDER(UsagePointProcessAssociationProvider.ASSOCIATION_TYPE, "Usage point"),
    METROLOGY_CONFIGURATION_PROPERTY("metrologyConfigurations", "Metrology configurations"),
    CONNECTION_STATE_PROPERTY("connectionStates", "Connection states"),
    CONNECTED("connectionState." + ConnectionState.CONNECTED.getId(), "Connected"),
    PHYSICALLY_DISCONNECTED("connectionState." + ConnectionState.PHYSICALLY_DISCONNECTED.getId(), "Physically disconnected"),
    LOGICALLY_DISCONNECTED("connectionState." + ConnectionState.LOGICALLY_DISCONNECTED.getId(), "Logically disconnected"),
    DEMOLISHED("connectionState." + ConnectionState.DEMOLISHED.getId(), "Demolished");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
