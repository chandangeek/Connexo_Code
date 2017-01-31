/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor.app.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum PrivilegeTranslationKeyPair implements TranslationKey {

    //Resources
    RESOURCE_COMMUNICATION_SERVER_MONITOR("communication.server.monitor", "Communication Server Monitor"),
    RESOURCE_COMMUNICATION_SERVER_MONITOR_DESCRIPTION("communication.server.monitor.description", "Access to the communication server's monitoring tool"),

    //Privileges
    MONITOR_COMMUNICATION_SERVER(MdcMonitorAppPrivileges.MONITOR_COMMUNICATION_SERVER, "Monitor communication server");

    private final String key;
    private final String description;

    PrivilegeTranslationKeyPair(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(PrivilegeTranslationKeyPair.values())
                .map(PrivilegeTranslationKeyPair::getKey)
                .collect(Collectors.toList())
                .toArray(new String[PrivilegeTranslationKeyPair.values().length]);
    }

}
