/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Optional;

public enum TranslationKeys implements TranslationKey {
    DATA_LIFE_CYCLE(Installer.DATA_LIFE_CYCLE_DESTINATION_NAME, Installer.DATA_LIFE_CYCLE_DISPLAY_NAME),
    CREATE_PARTITIONS(Installer.CREATE_PARTITIONS_DESTINATION_NAME, Installer.CREATE_PARTITIONS_DISPLAY_NAME);

    private final String key;
    private final String defaultFormat;

    public static Optional<TranslationKeys> getTranslationKey(String key) {
        for (TranslationKeys translationKey : TranslationKeys.values()) {
            if (translationKey.getKey().equals(key)) {
                return Optional.of(translationKey);
            }
        }
        return Optional.empty();
    }

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