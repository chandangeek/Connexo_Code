package com.energyict.mdc.channels.nls;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Contains all the {@link TranslationKey}s for the properties (and descriptions) of all the connection types.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-27 (10:28)
 */
public enum PropertyTranslationKeys implements TranslationKey {
    EIWEB_PLUS("upl.property.ipAddress", "IP address"),
    EIWEB_PLUS_DESCRIPTION("upl.property.description.ipAddress", "IP address");

    private final String key;
    private final String defaultFormat;

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
}