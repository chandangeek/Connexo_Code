package com.energyict.protocols.impl.channels;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.protocols.impl.channels.serial.optical.dlms.LegacyOpticalDlmsConnectionProperties;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-08 (13:52)
 */
public enum TranslationKeys implements TranslationKey {
    LEGACY_OPTICAL_DLMS_ADDRESSING_MODE("LegacyOpticalDlms." + LegacyOpticalDlmsConnectionProperties.Field.ADDRESSING_MODE.javaName(), "Addressing mode");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
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