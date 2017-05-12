/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 4/12/17.
 */
public enum KeyAccessorStatus implements TranslationKey {
    COMPLETE("status.complete", "Complete"),
    INCOMPLETE("status.incomplete", "Incomplete");

    private final String key;
    private final String defaultFormat;

    KeyAccessorStatus(String key, String defaultFormat) {
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
