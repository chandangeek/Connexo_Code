package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Provides the translation keys for the search properties
 * that are supported by the metering bundle.
 *
 */
public enum EDPropertyTranslationKeys implements TranslationKey {

    ENDDEVICE_MRID("enddevice.mRID", "mRID"),
    ENDDEVICE_NAME("enddevice.name", "Name"),

    
    ENDDEVICE_DOMAIN("enddevice.domain", "End Device"),
    ;

    private String key;
    private String defaultFormat;

    EDPropertyTranslationKeys(String key, String defaultFormat) {
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