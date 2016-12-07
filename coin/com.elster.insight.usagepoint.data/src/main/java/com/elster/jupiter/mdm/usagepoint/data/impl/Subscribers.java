package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.nls.TranslationKey;

public enum Subscribers implements TranslationKey {
    BULK_ITEMIZER(UsagePointDataService.BULK_ITEMIZER_QUEUE_SUBSCRIBER, "Usage Point bulk itemizer"),
    BULK_HANDLER(UsagePointDataService.BULK_HANDLING_QUEUE_SUBSCRIBER, "Usage Point bulk handler");

    private final String key;
    private final String defaultFormat;

    Subscribers(String key, String defaultFormat) {
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
