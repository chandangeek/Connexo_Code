/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search.enddevice;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Provides the translation keys for the search properties
 * that are supported by the metering bundle.
 *
 */
public enum PropertyTranslationKeys implements TranslationKey {

    ENDDEVICE_MRID("enddevice.mRID", "MRID"),
    ENDDEVICE_NAME("enddevice.name", "Name"),


    ENDDEVICE_DOMAIN("enddevice.domain", "Device"),
    ;

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
        return thesaurus.getFormat(this).format();
    }
}
