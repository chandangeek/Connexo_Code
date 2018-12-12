/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import com.elster.jupiter.nls.TranslationKey;

public enum DefaultState {
    ACTIVE(Translation.ACTIVE),
    DEMOLISHED(Translation.DEMOLISHED),
    INACTIVE(Translation.INACTIVE),
    UNDER_CONSTRUCTION(Translation.UNDER_CONSTRUCTION);

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

    private enum Translation implements TranslationKey {
        ACTIVE("usage.point.state.active", "Active"),
        DEMOLISHED("usage.point.state.demolished", "Demolished"),
        INACTIVE("usage.point.state.inactive", "Inactive"),
        UNDER_CONSTRUCTION("usage.point.state.under.construction", "Under construction");

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
