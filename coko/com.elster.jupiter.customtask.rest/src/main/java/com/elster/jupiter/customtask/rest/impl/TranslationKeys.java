/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    NONE("customtask.recurrence.none", "None"),
    SINCE("customtask.status.before", "{0} since"),
    ON("customtask.status.on", "{0} on"),
    SCHEDULED("customtask.occurrence.scheduled", "Scheduled"),
    ON_REQUEST("customtask.occurrence.onrequest", "On request"),
    NONRECURRING("customtask.occurrence.nonrecurring", "Non-recurring");

    private String key;
    private String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
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

    String translate(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }

}