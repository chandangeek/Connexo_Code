package com.energyict.mdc.rest.impl.comserver;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    ERROR("Error", "Error"),
    WARN("Warning", "Warning"),
    INFO("Information", "Information"),
    DEBUG("Debug", "Debug"),
    TRACE("Trace", "Trace"),
    COMSERVER_ONLINE("comserver.online", "Online"),
    COMSERVER_OFFLINE("comserver.offline", "Offline"),
    COMSERVER_REMOTE("comserver.remote", "Remote");

    private final String displayName;
    private final String defaultFormat;

    TranslationKeys(String displayName, String defaultFormat) {
        this.displayName = displayName;
        this.defaultFormat = defaultFormat;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(this.displayName, this.defaultFormat);
    }

    @Override
    public String toString() {
        return getKey();
    }

    @Override
    public String getKey() {
        return this.displayName;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }
}
