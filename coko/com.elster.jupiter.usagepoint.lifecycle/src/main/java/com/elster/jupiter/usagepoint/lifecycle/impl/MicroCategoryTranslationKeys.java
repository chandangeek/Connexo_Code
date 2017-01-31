/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum MicroCategoryTranslationKeys implements TranslationKey {
    CONNECTION_STATE_NAME(Keys.NAME_PREFIX + MicroCategory.CONNECTION_STATE.name(), "Connection state"),
    INSTALLATION_NAME(Keys.NAME_PREFIX + MicroCategory.INSTALLATION.name(), "Installation"),;

    private final String key;
    private final String defaultFormat;

    MicroCategoryTranslationKeys(String key, String defaultFormat) {
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

    public static class Keys {
        public static String NAME_PREFIX = "usage.point.micro.category.name.";

        private Keys() {
        }
    }
}
