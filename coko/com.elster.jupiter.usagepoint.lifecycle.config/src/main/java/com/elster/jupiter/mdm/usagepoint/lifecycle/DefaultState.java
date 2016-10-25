package com.elster.jupiter.mdm.usagepoint.lifecycle;

import com.elster.jupiter.nls.TranslationKey;

public enum DefaultState {
    CONNECTED(Translation.CONNECTED),
    DEMOLISHED(Translation.DEMOLISHED),
    LOGICALLY_DISCONNECTED(Translation.LOGICALLY_DISCONNECTED),
    PHYSICALLY_DISCONNECTED(Translation.PHYSICALLY_DISCONNECTED),
    UNDER_CONSTRUCTION(Translation.UNDER_CONSTRUCTION),;

    private final TranslationKey translation;

    DefaultState(TranslationKey translation) {
        this.translation = translation;
    }

    public String getKey() {
        return this.translation.getKey();
    }

    public TranslationKey getTranslation() {
        return translation;
    }

    enum Translation implements TranslationKey {
        CONNECTED("usage.point.state.connected", "Connected"),
        DEMOLISHED("usage.point.state.demolished", "Demolished"),
        LOGICALLY_DISCONNECTED("usage.point.state.logically.disconnected", "Logically disconnected"),
        PHYSICALLY_DISCONNECTED("usage.point.state.physically.disconnected", "Physically disconnected"),
        UNDER_CONSTRUCTION("usage.point.state.under.construction", "Under construction"),;

        private String key;
        private String defaultFormat;

        Translation(String key, String defaultFormat) {
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
}
