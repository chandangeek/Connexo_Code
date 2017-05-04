/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.meterdata.Device;

public enum TranslationKeys implements TranslationKey {

    DIALECT_CPS_DOMAIN_NAME(DeviceProtocolDialectPropertyProvider.class.getName(), "Device protocol dialect"),
    SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME(Device.class.getName(), "Security property set");

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