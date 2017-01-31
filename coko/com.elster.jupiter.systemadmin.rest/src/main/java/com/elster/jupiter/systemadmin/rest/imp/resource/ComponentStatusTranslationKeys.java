/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.system.ComponentStatus;

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
        return "component.status." + this.componentStatus.name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static ComponentStatusTranslationKeys getTranslatedName(ComponentStatus componentStatus) {
        return Arrays.stream(ComponentStatusTranslationKeys.values()).filter(key -> key.componentStatus == componentStatus).findFirst().orElse(ACTIVE);
    }
}
