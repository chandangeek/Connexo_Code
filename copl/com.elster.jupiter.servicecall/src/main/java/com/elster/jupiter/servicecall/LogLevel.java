package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 2/10/16.
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
