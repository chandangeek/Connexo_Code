package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    LIFE_CYCLE_NAME("usage.point.life.cycle.name", "Standard usage point lifecycle"),
    TRANSITION_INSTALL_ACTIVE("usage.point.transition.install.active", "Install active"),
    TRANSITION_INSTALL_INACTIVE("usage.point.transition.install.inactive", "Install inactive"),
    TRANSITION_DEACTIVATE("usage.point.transition.deactivate", "Deactivate"),
    TRANSITION_ACTIVATE("usage.point.transition.activate", "Activate"),
    TRANSITION_DEMOLISH_FROM_ACTIVE("usage.point.transition.demolish.from.active", "Demolish"),
    TRANSITION_DEMOLISH_FROM_INACTIVE("usage.point.transition.demolish.from.inactive", "Demolish"),
    ;

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
}
