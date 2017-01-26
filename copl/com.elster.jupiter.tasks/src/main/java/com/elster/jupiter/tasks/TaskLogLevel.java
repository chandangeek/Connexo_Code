package com.elster.jupiter.tasks;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum TaskLogLevel implements TranslationKey {
    // The ordinal of this enum is stored in the database. So, always add new enums at the end!
    ERROR("Error"),
    WARNING("Warning"),
    INFORMATION("Information"),
    DEBUG("Debug"),
    TRACE("Trace");

    private String name;

    TaskLogLevel(String name) {
        this.name = name;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(this.getKey(), this.getDefaultFormat());
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getKey() {
        return "task.loglevel." + this.name().toLowerCase();
    }

    @Override
    public String getDefaultFormat() {
        return toString();
    }
}