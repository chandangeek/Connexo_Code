package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum  TranslationKeys implements TranslationKey {
    ACTIVE("active", "Active"),
    INACTIVE("inactive", "Inactive"),
    YES("yes", "Yes"),
    NO("no", "-"),
    NAME("name", "Name"),
    STATUS("status", "Status"),
    DAYLIMIT("dayLimit", "Day limit"),
    WEEKLIMIT("weekLimit", "Week limit"),
    MONTHLIMIT("monthLimit", "Month limit"),
    REMOVED("removed", "Removed");
    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static TranslationKeys fromActive(boolean active) {
        return active ? ACTIVE : INACTIVE;
    }

    public static TranslationKeys fromCommandActive(boolean commandActive) {
        return commandActive ? YES : NO;
    }
}
