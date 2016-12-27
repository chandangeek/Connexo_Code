package com.energyict.mdc.channels;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-27 (10:28)
 */
public enum TranslationKeys implements TranslationKey {
    EIWEB_PLUS("ipAddress", "IP address"),
    EIWEB_PLUS_DESCRIPTION("ipAddress.description", "IP address");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
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