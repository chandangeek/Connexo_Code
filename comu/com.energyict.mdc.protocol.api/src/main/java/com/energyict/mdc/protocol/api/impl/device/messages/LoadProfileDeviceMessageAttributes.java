package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

/**
 * Copyrights EnergyICT
 * Date: 30.04.15
 * Time: 15:35
 */
public enum LoadProfileDeviceMessageAttributes implements TranslationKey {

    loadProfileAttributeName("LoadProfileDeviceMessage." + DeviceMessageConstants.loadProfileAttributeName, "LoadProfile"),
    LoadProfileMessageFromDate("LoadProfileDeviceMessage." + DeviceMessageConstants.fromDateAttributeName, "From"),
    LoadProfileMessageToDate("LoadProfileDeviceMessage." + DeviceMessageConstants.toDateAttributeName, "To"),
    ;

    private final String key;
    private final String defaultFormat;

    LoadProfileDeviceMessageAttributes(String key, String defaultFormat) {
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