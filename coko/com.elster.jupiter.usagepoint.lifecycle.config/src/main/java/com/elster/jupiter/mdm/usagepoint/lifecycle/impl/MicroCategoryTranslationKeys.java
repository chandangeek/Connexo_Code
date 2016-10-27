package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCategory;
import com.elster.jupiter.nls.TranslationKey;

public enum MicroCategoryTranslationKeys implements TranslationKey {
    SERVICE_CALLS_NAME(Keys.NAME_PREFIX + MicroCategory.SERVICE_CALLS.name(), "Service calls"),
    VALIDATION_NAME(Keys.NAME_PREFIX + MicroCategory.VALIDATION.name(), "Validation"),;

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
