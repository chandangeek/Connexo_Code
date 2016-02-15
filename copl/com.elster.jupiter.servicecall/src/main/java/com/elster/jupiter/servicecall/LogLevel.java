package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 2/10/16.
 */
public enum LogLevel implements TranslationKey {
    SEVERE("SEVERE"),
    WARNING("WARNING"),
    INFO("INFO"),
    CONFIG("CONFIG"),
    FINE("FINE"),
    FINER("FINER"),
    FINEST("FINEST");

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
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return toString();
    }
}
