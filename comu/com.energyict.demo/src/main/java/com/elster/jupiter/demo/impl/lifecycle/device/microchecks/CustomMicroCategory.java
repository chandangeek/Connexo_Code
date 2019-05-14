/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.lifecycle.device.microchecks;

import com.elster.jupiter.nls.TranslationKey;

public enum CustomMicroCategory implements TranslationKey {
    MAINTENANCE("demo.lifecycle.device.microchecks.category.maintenance", "Maintenance");

    private final String key;
    private final String defaultFormat;

    CustomMicroCategory(String key, String defaultFormat) {
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
