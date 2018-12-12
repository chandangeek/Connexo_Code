/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.nls.TranslationKey;

public enum DataValidationTaskStatus implements TranslationKey {
    BUSY("ongoing", "Ongoing"),
    SUCCESS("successful", "Successful"),
    WARNING("warning", "Warning"),
    FAILED("failed", "Failed"),
    NOT_PERFORMED("created", "Created");

    private final String key;
    private final String defaultFormat;

    DataValidationTaskStatus(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return "validation.task.status." + this.key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public String toString() {
        return this.key;
    }

}