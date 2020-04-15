/*
 *
 *  * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 11/6/2019 (14:51)
 */
public enum TranslationKeys implements TranslationKey {

    ACTIVE("Active", "Active"),
    OBJECTS("tooltip.objects.lost", "Objects: {0}");

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
}
