package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Provides the translation keys for ReadingTypes
 */
public enum ReadingTypeTranslationKeys implements TranslationKey {

    PRIMARY("readingType.primary", "Primary"),
    SECONDARY("readingType.secondary", "Secondary"),

    ;

    private String key;
    private String defaultFormat;

    ReadingTypeTranslationKeys(String key, String defaultFormat) {
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

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(this.key, this.defaultFormat);
    }
}
