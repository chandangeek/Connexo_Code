/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;


public enum TranslationKeys implements TranslationKey {
    SCHEDULED("recurrenttask.occurrence.scheduled", "Scheduled"),
    MDC("MultiSense", "MultiSense"),
    MDM("Insight", "Insight"),
    FACTS("Facts", "Facts"),
    ADMIN("Admin", "Admin"),
    FLOW("Flow", "Flow")
    ;

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

    public String getTranslated(Thesaurus thesaurus, Object... args){
        if (thesaurus == null) {
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        return thesaurus.getFormat(this).format(args);
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
