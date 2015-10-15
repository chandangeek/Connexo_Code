package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum UsagePointConnectedKind implements TranslationKey {
    UNKNOWN("Unknown", "Unknown"),
    CONNECTED("connected", "Connected"),
    PHYSICALLYDISCONNECTED("physically.disconnected", "Physically disconnected"),
    LOGICALLYDISCONNECTED("logically.disconnected", "Logically disconnected");

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
        return thesaurus.getString(this.value, this.defaultFormat);
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