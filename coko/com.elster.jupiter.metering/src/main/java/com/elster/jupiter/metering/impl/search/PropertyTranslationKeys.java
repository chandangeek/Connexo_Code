package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Provides the translation keys for the search properties
 * that are supported by the metering bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (15:05)
 */
public enum PropertyTranslationKeys implements TranslationKey {

    USAGEPOINT_MRID("usagepoint.mRID", "mRID");

    private String key;
    private String defaultFormat;

    PropertyTranslationKeys(String key, String defaultFormat) {
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