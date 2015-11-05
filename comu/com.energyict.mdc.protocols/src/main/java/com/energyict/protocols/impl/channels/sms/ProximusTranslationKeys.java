package com.energyict.protocols.impl.channels.sms;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (09:11)
 */
public enum ProximusTranslationKeys implements TranslationKey {

    INBOUND_CUSTOM_PROPERTY_SET_NAME("InboundProximusSmsCustomPropertySet", "Inbound proximus"),
    OUTBOUND_CUSTOM_PROPERTY_SET_NAME("OutboundProximusSmsCustomPropertySet", "Outbound proximus");

    private final String key;
    private final String defaultFormat;

    ProximusTranslationKeys(String key, String defaultFormat) {
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