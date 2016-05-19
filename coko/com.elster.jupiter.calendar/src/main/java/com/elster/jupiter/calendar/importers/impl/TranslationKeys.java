package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    CALENDAR_IMPORTER(CalendarImporterFactory.NAME, "Calendar importer"),
    CALENDAR_IMPORTED_SUCCESSFULLY("calendar.import.summary.succeeded", "Successfull completed."),
    CALENDAR_IMPORT_FAILED("calendar.import.summary.failed", "Failed to complete, no calendar has been processed.")
    ;

    private final String key;
    private final String defaultFormat;

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
}
