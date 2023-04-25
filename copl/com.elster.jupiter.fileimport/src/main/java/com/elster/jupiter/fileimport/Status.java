/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Enumeration of the states of a FileImportOccurrence.
 * A FileImportOccurrence starts out as new.
 * When moved to processing becomes PROCESSING
 * and upon completion becomes either SUCCESS or FAILURE,
 * depending on the success of processing.
 */
public enum Status implements TranslationKey {
    NEW("New"),
    PROCESSING("Ongoing"),
    // starting from here, only final statuses
    SUCCESS("Successful"),
    SUCCESS_WITH_FAILURES("Partial success"),
    FAILURE("Failed");

    private final String defaultFormat;

    Status(String name) {
        this.defaultFormat = name;
    }

    @Override
    public String toString() {
        return this.defaultFormat;
    }

    @Override
    public String getKey() {
        return "com.elster.jupiter.fileimport." + name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public boolean isFinal() {
        return ordinal() >= Status.SUCCESS.ordinal();
    }
}
