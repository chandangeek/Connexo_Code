package com.energyict.protocolimplv2.abnt;

import com.elster.jupiter.nls.TranslationKey;

import com.energyict.protocolimplv2.abnt.common.AbntProperties;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-10 (10:08)
 */
public enum AbntTranslationKeys implements TranslationKey {

    READER_SERIAL_NUMBER("ABNT." + AbntProperties.READER_SERIAL_NUMBER_PROPERTY, "Reader serial number"),
    RETRIES("ABNT.dialect.retries", "Retries"),
    TIMEOUT("ABNT.dialect.timeout", "Timeout"),
    FORCED_DELAY("ABNT.dialect.forcedDelay", "Forced delay"),
    DELAY_AFTER_ERROR("ABNT.dialect.delayAfterError", "Delay after error"),
    ;

    private final String key;
    private final String defaultFormat;

    AbntTranslationKeys(String key, String defaultFormat) {
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