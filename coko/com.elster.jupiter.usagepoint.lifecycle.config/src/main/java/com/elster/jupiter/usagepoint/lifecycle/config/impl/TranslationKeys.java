package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    TRANSITION_INSTALL("transition.install", "Install"),
    TRANSITION_SEAL("transition.seal", "Seal"),
    TRANSITION_OPEN("transition.open", "Open"),
    TRANSITION_DEMOLISH_FROM_CONNECTED("transition.demolish.from.connected", "Demolish"),
    TRANSITION_DEMOLISH_FROM_PHYSICALLY_DISCONNECTED("transition.demolish.from.physically.disconnecteD", "Demolish"),;

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
