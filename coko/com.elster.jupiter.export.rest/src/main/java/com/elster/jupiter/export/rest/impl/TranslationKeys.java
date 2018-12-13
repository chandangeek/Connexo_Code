/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-29 (13:40)
 */
public enum TranslationKeys implements TranslationKey {
    NONE("exporttask.recurrence.none", "None"),
    SINCE("status.before", "{0} since"),
    ON("status.on", "{0} on"),
    SCHEDULED("dataexporttask.occurrence.scheduled", "Scheduled"),
    ON_REQUEST("dataexporttask.occurrence.onrequest", "On request"),
    NONRECURRING("dataexporttask.occurrence.nonrecurring", "Non-recurring");

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