/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.nls.TranslationKey;

public enum DataExportStatus implements TranslationKey {
    BUSY("Ongoing"),
    SUCCESS("Successful"),
    WARNING("Warning"),
    FAILED("Failed"),
    NOT_PERFORMED("Created");

    private final String defaultFormat;

    DataExportStatus(String defaultFormat) {
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