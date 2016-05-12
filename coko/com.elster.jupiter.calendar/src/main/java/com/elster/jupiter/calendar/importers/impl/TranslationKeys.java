package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    //Translations for importer names
    CALENDAR_IMPORTER(CalendarImporterFactory.NAME, "Calendar importer"),
    TOU_CALENDAR_IMPORTED_SUCCESSFULLY("calendar.import.succes", "Time of use calendar \"{0}\" is imported successfully."),
    TOU_CALENDAR_IMPORT_FAILED("calendar.import.failed", "Time of use calendar import has failed.")
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
