/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.nls.TranslationKey;

public enum MetrologyConfigurationStatus {

    INACTIVE("inactive", Translation.INACTIVE),
    ACTIVE("active", Translation.ACTIVE),
    DEPRECATED("deprecated", Translation.DEPRECATED),;

    private String id;
    private TranslationKey translationKey;

    MetrologyConfigurationStatus(String id, TranslationKey translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public String getId() {
        return id;
    }

    public TranslationKey getTranslationKey() {
        return this.translationKey;
    }

    public enum Translation implements TranslationKey {

        INACTIVE("metrologyConfiguration.status.inactive", "Inactive"),
        ACTIVE("metrologyConfiguration.status.active", "Active"),
        DEPRECATED("metrologyConfiguration.status.deprecated", "Deprecated");

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


