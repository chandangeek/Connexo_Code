package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    LIFE_CYCLE_KIND_INTERVAL (Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.INTERVAL.name(), "Interval data"),
    LIFE_CYCLE_KIND_DAILY (Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.DAILY.name(), "Day / Month profiles"),
    LIFE_CYCLE_KIND_REGISTER (Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.REGISTER.name(), "Register data"),
    LIFE_CYCLE_KIND_ENDDEVICEEVENT (Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.ENDDEVICEEVENT.name(), "Event data"),
    LIFE_CYCLE_KIND_LOGGING (Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.LOGGING.name(), "Logging data"),
    LIFE_CYCLE_KIND_JOURNAL (Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + LifeCycleCategoryKind.JOURNAL.name(), "Journal tables"),
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

    public static class Constants {
        public static final String DATA_LIFECYCLE_CATEGORY_NAME_PREFIX = "data.lifecycle.category.";
    }
}
