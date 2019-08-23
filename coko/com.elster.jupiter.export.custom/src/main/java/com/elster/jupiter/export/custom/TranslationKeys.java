/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.custom;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    NUMBER_OF_DATASOURCES_SELECTED("dataexport.dataSourcesSelected", "{0} data source(s) selected"),
    NUMBER_OF_DATASOURCES_SKIPPED("dataexport.dataSourcesSkipped", "{0} data source(s) skipped"),
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
