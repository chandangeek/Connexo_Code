/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Sorted list of known log levels
 * Created by bvn on 2/10/16.
 */
public enum LogLevel implements TranslationKey {
    SEVERE("Severe"),
    WARNING("Warning"),
    INFO("Information"),
    CONFIG("Configuration"),
    FINE("Fine"),
    FINER("Finer"),
    FINEST("Finest");

    private String name;

    LogLevel(String name) {
        this.name = name;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(getTranslationKey(this), this.getDefaultFormat());
    }

    public static String getTranslationKey(LogLevel ll){
        return "servicecalltype.loglevel." + ll.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getKey() {
        return getTranslationKey(this);
    }

    @Override
    public String getDefaultFormat() {
        return toString();
    }
}
