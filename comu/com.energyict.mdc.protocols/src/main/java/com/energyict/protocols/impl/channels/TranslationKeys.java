/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.protocols.impl.channels.serial.optical.dlms.LegacyOpticalDlmsConnectionProperties;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-08 (13:52)
 */
public enum TranslationKeys implements TranslationKey {
    CONNECTION_PROVIDER_DOMAIN_NAME(ConnectionProvider.class.getName(), "Connection method"),
    LEGACY_OPTICAL_DLMS_ADDRESSING_MODE("LegacyOpticalDlms." + LegacyOpticalDlmsConnectionProperties.Field.ADDRESSING_MODE.javaName(), "Addressing mode"),
    DATA_LINK_LAYER_TYPE("LegacyOpticalDlms.DATA_LINK_LAYER_TYPE", "Data link layer type"),
    SERVER_MAC_ADDRESS("LegacyOpticalDlms.SERVER_MAC_ADDRESS", "Server mac address"),
    SERVER_LOWER_MAC_ADDRESS("LegacyOpticalDlms.SERVER_LOWER_MAC_ADDRESS", "Server lower mac address"),
    SERVER_UPPER_MAC_ADDRESS("LegacyOpticalDlms.SERVER_UPPER_MAC_ADDRESS", "Server upper mac address"),
    ;

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