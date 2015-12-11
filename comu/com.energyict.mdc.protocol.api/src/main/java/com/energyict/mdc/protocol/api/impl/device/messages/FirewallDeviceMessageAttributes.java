package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

/**
 * Copyrights EnergyICT
 * Date: 30.04.15
 * Time: 15:35
 */
public enum FirewallDeviceMessageAttributes implements TranslationKey {

    EnableDLMS(DeviceMessageConstants.EnableDLMS, "Enable DLMS"),
    EnableHTTP(DeviceMessageConstants.EnableHTTP, "Enable HTTP"),
    EnableSSH(DeviceMessageConstants.EnableSSH, "Enable SSH"),
    defaultEnabled(DeviceMessageConstants.defaultEnabled, "Default enabled"),
    ;

    private final String key;
    private final String defaultFormat;

    FirewallDeviceMessageAttributes(String key, String defaultFormat) {
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