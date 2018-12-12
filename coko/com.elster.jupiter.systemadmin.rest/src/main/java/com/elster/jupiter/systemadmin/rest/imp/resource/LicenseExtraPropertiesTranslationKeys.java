/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.nls.TranslationKey;

public enum LicenseExtraPropertiesTranslationKeys implements TranslationKey {
    DEVICE_LIMIT("deviceLimit", "Device limit"),
    OPERATOR_LIMIT("operatorLimit", "operator limit"),
    OPERATOR_LIMIT_2("OperatorLimit", "Operator limit"),
    HFREADINGTYPELIMIT("HFReadingTypeLimit","HF reading type limit"),
    LFREADINGTYPELIMIT("LFReadingTypeLimit","LF reading type limit"),
    CLIENT_LIMIT("clientLimit", "Client limit"),
    VALIDATION("validation", "Validation"),
    PROTOCOLS("protocols", "Protocols"),
    PROTOCOL_FAMILIES("protocolFamilies", "Protocol families"),
    LICENSOR("licensor", "Licensor"),
    LICENSEE("licensee", "Licensee");

    private final String key;
    private final String defaultFormat;

    LicenseExtraPropertiesTranslationKeys(String key, String defaultFormat) {
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
