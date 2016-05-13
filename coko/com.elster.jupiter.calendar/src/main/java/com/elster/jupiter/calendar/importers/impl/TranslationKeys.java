package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    CALENDAR_IMPORTER(CalendarImporterFactory.NAME, "Calendar importer"),
    TOU_CALENDAR_IMPORTED_SUCCESSFULLY("calendar.import.succes", "Time of use calendar \"{0}\" is imported successfully."),
    TOU_CALENDAR_IMPORT_FAILED_XML_OK("calendar.import.failed.xml.ok", "Time of use calendar import has failed."),
    TOU_CALENDAR_IMPORT_FAILED_XML_NOT_OK("calendar.import.failed.xml.nok", "Time of use calendar import has failed (validation of the file failed).");
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
