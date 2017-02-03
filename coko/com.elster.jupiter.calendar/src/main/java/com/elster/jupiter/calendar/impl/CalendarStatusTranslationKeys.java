/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Status;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

public enum CalendarStatusTranslationKeys implements TranslationKey {

    ACTIVE(Status.ACTIVE, "Active"),
    INACTIVE(Status.INACTIVE, "Inactive");

    private Status status;
    private String defaultFormat;

    CalendarStatusTranslationKeys(Status status, String defaultFormat) {
        this.status = status;
        this.defaultFormat = defaultFormat;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String getKey() {
        return Status.class.getSimpleName() + "." + this.status.name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static CalendarStatusTranslationKeys from(Status status) {
        return Stream
                .of(values())
                .filter(each -> each.status.equals(status))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Translation missing for calendar status : " + status));
    }

    public static String translationFor(Status status, Thesaurus thesaurus) {
        return thesaurus.getFormat(from(status)).format();
    }
}
