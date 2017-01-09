package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.meterdata.Device;

/**
 * Copyrights EnergyICT
 * Date: 16/05/14
 * Time: 09:12
 */
public enum TranslationKeys implements TranslationKey {

    DEVICEDIALHOMEID("deviceDialHomeId", "Device call home ID"),
    TIMEOUT("protocol.timeout", "Timeout"),
    RETRIES("protocol.retries", "Retries"),
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