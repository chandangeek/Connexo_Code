package com.energyict.protocols.impl.channels.ip;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (09:11)
 */
public enum IpTranslationKeys implements TranslationKey {

    OUTBOUND_TCP_CUSTOM_PROPERTY_SET_NAME("OutboundIpCustomPropertySet", "Outbound TCP/IP"),
    OUTBOUND_TCP_POST_DIAL_CUSTOM_PROPERTY_SET_NAME("OutboundIpPostDialCustomPropertySet", "Outbound TCP/IP (post dial)"),
    OUTBOUND_UDP_CUSTOM_PROPERTY_SET_NAME("OutboundUdpCustomPropertySet", "Outbound UDP");

    private final String key;
    private final String defaultFormat;

    IpTranslationKeys(String key, String defaultFormat) {
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