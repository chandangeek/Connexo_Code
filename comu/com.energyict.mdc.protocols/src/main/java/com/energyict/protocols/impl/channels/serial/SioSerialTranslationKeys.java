package com.energyict.protocols.impl.channels.serial;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (09:11)
 */
public enum SioSerialTranslationKeys implements TranslationKey {

    CUSTOM_PROPERTY_SET_NAME("SioSerialCustomPropertySet", "Sio serial");

    private final String key;
    private final String defaultFormat;

    SioSerialTranslationKeys(String key, String defaultFormat) {
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