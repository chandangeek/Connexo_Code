/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.impl.importers.CalendarImporterFactory;
import com.elster.jupiter.calendar.impl.importers.CalendarImporterMessageHandler;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by igh on 18/04/2016.
 */
public enum TranslationKeys implements TranslationKey {
    CALENDAR_IMPORTER(CalendarImporterFactory.NAME, "Calendar importer"),
    CALENDAR_IMPORTED_SUCCESSFULLY("calendar.import.summary.succeeded", "Finished successfully."),
    CALENDAR_IMPORT_FAILED("calendar.import.summary.failed", "Failed to complete, no calendar has been processed."),
    CALENDAR_IMPORTER_MESSAGE_HANDLER_DISPLAYNAME(CalendarImporterMessageHandler.SUBSCRIBER_NAME, "Handle calendar import"),
    CALENDAR_CATEGORY_TOU("calendar.category." + "tou", "Time of use"),
    CALENDAR_CATEGORY_WORKFORCE("calendar.category." + "workforce", "Workforce"),
    CALENDAR_CATEGORY_COMMANDS("calendar.category." + "commands", "Commands"),
    RECURRENT_TASK(CalendarTimeSeriesExtenderHandlerFactory.TASK_SUBSCRIBER, CalendarTimeSeriesExtenderHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME);

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