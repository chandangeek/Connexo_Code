/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (14:44)
 */
public enum CustomPropertySetTranslationKeys implements TranslationKey {

    NO_OR_PASSWORD_CUSTOM_PROPERTY_SET_NAME("NoOrPasswordSecurity", "No security or password protected security"),
    BASIC_AUTHENTICATION_CUSTOM_PROPERTY_SET_NAME("BasicAuthentication", "Basic authentication"),
    DLMS_SECURITY_CUSTOM_PROPERTY_SET_NAME("DlmsSecurity", "DLMS security"),
    DLMS_SECURITY_PER_CLIENT_CUSTOM_PROPERTY_SET_NAME("DlmsSecurityPerClient", "DLMS security per client"),
    ANSI_C12_CUSTOM_PROPERTY_SET_NAME("AnsiC12Security", "ANSI C12 security"),
    WAVENIS_CUSTOM_PROPERTY_SET_NAME("WavenisSecurity", "Wavenis security"),
    IEC1107_CUSTOM_PROPERTY_SET_NAME("IEC1107Security", "IEC1107 security"),
    MTU155_CUSTOM_PROPERTY_SET_NAME("MTU155Security", "MTU155 security"),
    EXTENDED_ANSI_C12_CUSTOM_PROPERTY_SET_NAME("ExtendedAnsiC12Security", "Extended ANSI C12 security");

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