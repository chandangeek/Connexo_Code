/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 5/3/16.
 */
public enum LogLevel implements TranslationKey {
    SEVERE("Severe"),
    WARNING("Warning"),
    INFO("Info"),
    CONFIG("Config"),
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

    public static String getTranslationKey(LogLevel ll) {
        return "webservices.loglevel." + ll.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return toString();
    }
}
