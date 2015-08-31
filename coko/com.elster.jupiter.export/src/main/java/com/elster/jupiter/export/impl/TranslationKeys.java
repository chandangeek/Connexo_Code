package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    RELATIVE_PERIOD_CATEGORY(Installer.RELATIVE_PERIOD_CATEGORY, "Data Export"),
    STANDARD_DATA_SELECTOR_FACTORY(StandardDataSelectorFactory.class.getName(), DataExportService.STANDARD_DATA_SELECTOR),
    SUBSCRIBER_NAME(Installer.SUBSCRIBER_NAME, DataExportServiceImpl.SUBSCRIBER_DISPLAYNAME),
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
