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
    REMOVED("removed", "Removed"),
    PENDING_ACTIVATION("pendingActivation", "Request for activation is pending and waiting for approval."),
    PENDING_DEACTIVATION("pendingDeactivation", "Request for deactivation is pending and waiting for approval."),
    PENDING_UPDATE("pendingUpdate", "Request for changes is pending and waiting for approval."),
    PENDING_REMOVAL("pendingRemoval", "Request for removal is pending and waiting for approval.");
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
