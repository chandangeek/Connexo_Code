package com.elster.jupiter.system;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum ComponentStatusTranslationKeys implements TranslationKey {
    ACTIVE(ComponentStatus.ACTIVE, "Active"),
    INSTALLED(ComponentStatus.INSTALLED, "Installed"),
    RESOLVED(ComponentStatus.RESOLVED, "Resolved"),
    STARTING(ComponentStatus.STARTING, "Starting"),
    STOPPING(ComponentStatus.STOPPING, "Stopping"),
    UNINSTALLED(ComponentStatus.UNINSTALLED, "Uninstalled");

    private final ComponentStatus componentStatus;
    private final String defaultFormat;

    ComponentStatusTranslationKeys(ComponentStatus componentStatus, String defaultFormat) {
        this.componentStatus = componentStatus;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.componentStatus.name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static ComponentStatusTranslationKeys getTranslatedName(ComponentStatus componentStatus) {
        return Arrays.stream(ComponentStatusTranslationKeys.values()).filter(key -> key.componentStatus.equals(componentStatus)).findFirst().orElse(ACTIVE);
    }
}
