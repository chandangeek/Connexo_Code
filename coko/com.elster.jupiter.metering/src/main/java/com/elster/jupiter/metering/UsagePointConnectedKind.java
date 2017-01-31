/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum UsagePointConnectedKind implements TranslationKey {
    UNKNOWN("connectionState.unknown", "Unknown"),
    CONNECTED("connectionState.connected", "Connected"),
    PHYSICALLYDISCONNECTED("connectionState.physicallyDisconnected", "Physically disconnected"),
    LOGICALLYDISCONNECTED("connectionState.logicallyDisconnected", "Logically disconnected");

    private final String value;
    private final String defaultFormat;

    UsagePointConnectedKind(String value, String defaultFormat) {
        this.value = value;
        this.defaultFormat = defaultFormat;
    }

    public static UsagePointConnectedKind get(int id) {
        return values()[id - 1];
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public String getKey() {
        return this.value;
    }

    public int getId() {
        return ordinal() + 1;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return defaultFormat;
    }
}