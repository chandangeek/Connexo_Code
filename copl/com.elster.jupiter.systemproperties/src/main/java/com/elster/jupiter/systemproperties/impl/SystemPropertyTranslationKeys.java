package com.elster.jupiter.systemproperties.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum SystemPropertyTranslationKeys implements TranslationKey {
    EVICTION_TIME("evictiontime", "Cache eviction time"),
    EVICTION_TIME_DESCRIPTION("evictionTimeDescription", "Eviction time for table cache"),
    ENABLE_CACHE("enablecache", "Enable caching"),
    ENABLE_CACHE_DESCRIPTION("enableCacheDescription", "Enable caching for specified tables"),;

    private final String key;
    private final String defaultFormat;

    SystemPropertyTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
