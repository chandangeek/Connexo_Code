/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;

import com.elster.jupiter.nls.TranslationKey;

public enum CustomTaskStatus implements TranslationKey {
    BUSY("Ongoing"),
    SUCCESS("Successful"),
    WARNING("Warning"),
    FAILED("Failed"),
    NOT_PERFORMED("Created");

    private final String defaultFormat;

    CustomTaskStatus(String defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}