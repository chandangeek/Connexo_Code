/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl.meteradvance;

import com.elster.jupiter.nls.TranslationKey;

enum TranslationKeys implements TranslationKey {

    REFERENCE_READING_TYPE(MeterAdvanceValidator.REFERENCE_READING_TYPE, "Reference reading type"),
    MAX_ABSOLUTE_DIFFERENCE(MeterAdvanceValidator.MAX_ABSOLUTE_DIFFERENCE, "Maximum absolute difference"),
    REFERENCE_PERIOD(MeterAdvanceValidator.REFERENCE_PERIOD, "Reference period"),
    MIN_THRESHOLD(MeterAdvanceValidator.MIN_THRESHOLD, "Minimum threshold");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return MeterAdvanceValidator.class.getName() + "." + key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }
}
