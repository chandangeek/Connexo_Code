/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Provides {@link TranslationKey}s for the names of the
 * {@link CustomPropertySet}s provided by this protocol bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:50)
 */
public enum CustomPropertySetTranslationKeys implements TranslationKey {

    // Connection Types
    EIWEB_CUSTOM_PROPERTY_SET_NAME("EIWebCustomPropertySet", "EIWeb"),
    EIWEB_PLUS_CUSTOM_PROPERTY_SET_NAME("EIWebPlusCustomPropertySet", "EIWeb+"),
    SIO_SERIAL_CUSTOM_PROPERTY_SET_NAME("SioSerialCustomPropertySet", "Sio serial"),
    LEGACY_OPTICAL_DLMS_CUSTOM_PROPERTY_SET_NAME("LegacyOpticalDlmsCustomPropertySet", "Legacy optical dlms"),
    CTR_INBOUND_DIAL_HOME_ID_CUSTOM_PROPERTY_SET_NAME("CTRInboundDialHomeIdCustomPropertySet", "CTR Inbound dial home id"),
    INBOUND_PROXIMUS_CUSTOM_PROPERTY_SET_NAME("InboundProximusSmsCustomPropertySet", "Inbound proximus"),
    OUTBOUND_PROXIMUS_CUSTOM_PROPERTY_SET_NAME("OutboundProximusSmsCustomPropertySet", "Outbound proximus"),
    OUTBOUND_TCP_CUSTOM_PROPERTY_SET_NAME("OutboundIpCustomPropertySet", "Outbound TCP/IP"),
    OUTBOUND_TCP_POST_DIAL_CUSTOM_PROPERTY_SET_NAME("OutboundIpPostDialCustomPropertySet", "Outbound TCP/IP (post dial)"),
    OUTBOUND_UDP_CUSTOM_PROPERTY_SET_NAME("OutboundUdpCustomPropertySet", "Outbound UDP");

    private final String key;
    private final String defaultFormat;

    CustomPropertySetTranslationKeys(String key, String defaultFormat) {
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