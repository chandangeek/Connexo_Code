/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum DefaultTranslationKey implements TranslationKey {
    INCOMPLETE("incomplete", "Incomplete"),
    COMPLETE("complete", "Complete"),
    RELATIVE_PERIOD_CATEGORY_USAGE_POINT_VALIDATION_OVERVIEW("relativeperiod.category.usagepoint.validationOverview", "Usage point validation overview"),;

    private String key;
    private String defaultFormat;

    DefaultTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }
}
