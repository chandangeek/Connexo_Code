package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    LIFE_CYCLE_NAME("usage.point.life.cycle.name", "Default"),
    TRANSITION_INSTALL("usage.point.transition.install", "Install"),
    TRANSITION_SEAL("usage.point.transition.seal", "Seal"),
    TRANSITION_OPEN("usage.point.transition.open", "Open"),
    TRANSITION_DEMOLISH_FROM_CONNECTED("usage.point.transition.demolish.from.connected", "Demolish"),
    TRANSITION_DEMOLISH_FROM_PHYSICALLY_DISCONNECTED("usage.point.transition.demolish.from.physically.disconnected", "Demolish"),;

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
