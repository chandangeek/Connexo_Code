/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    RELATIVE_PERIOD_CATEGORY(Installer.RELATIVE_PERIOD_CATEGORY, "Data Export"),
    RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY(Installer.RELATIVE_PERIOD_UPDATEWINDOW_CATEGORY, "Update window"),
    RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY(Installer.RELATIVE_PERIOD_UPDATETIMEFRAME_CATEGORY, "Update timeframe"),
    STANDARD_DATA_SELECTOR_FACTORY(StandardDataSelectorFactory.class.getName(), DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR),
    SUBSCRIBER_NAME(Installer.SUBSCRIBER_NAME, DataExportServiceImpl.SUBSCRIBER_DISPLAY_NAME),

    NUMBER_OF_DATASOURCES_SUCCESSFULLY_EXPORTED("dataexport.dataSourcesSuccessfullyExported", "{0} data source(s) successfully exported"),
    NUMBER_OF_DATASOURCES_SKIPPED("dataexport.dataSourcesSkipped", "{0} data source(s) skipped"),
    NUMBER_OF_DEVICES_WITH_EVENTS_SUCCESSFULLY_EXPORTED("dataexport.devicesWithEventsSuccessfullyExported", "{0} device(s) with events successfully exported"),
    NUMBER_OF_EVENTS_EXPORTED("dataexport.eventsSuccessfullyExported", "{0} event(s) successfully exported")
   ;

    private String key;
    private String defaultFormat;

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
