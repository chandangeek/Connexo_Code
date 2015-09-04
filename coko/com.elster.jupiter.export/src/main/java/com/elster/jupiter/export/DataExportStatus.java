package com.elster.jupiter.export;

import com.elster.jupiter.nls.TranslationKey;

public enum DataExportStatus implements TranslationKey {
    BUSY("Busy"),
    SUCCESS("Success"),
    WARNING("Warning"),
    FAILED("Failed"),
    NOT_PERFORMED("Not performed yet");

    private String name;

    DataExportStatus(String name) {
        this.name = name;
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
