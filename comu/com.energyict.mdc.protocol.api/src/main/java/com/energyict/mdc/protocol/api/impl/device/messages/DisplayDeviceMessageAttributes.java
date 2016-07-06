package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

/**
 * Copyrights EnergyICT
 * Date: 30.04.15
 * Time: 15:35
 */
enum DisplayDeviceMessageAttributes implements TranslationKey {

    p1InformationAttributeName(DeviceMessageConstants.p1InformationAttributeName, "Consumer p1"),
    DisplayMessageAttributeName(DeviceMessageConstants.DisplayMessageAttributeName, "Display message"),
    DisplayMessageTimeDurationAttributeName(DeviceMessageConstants.DisplayMessageTimeDurationAttributeName, "Time duration"),
    DisplayMessageActivationDate(DeviceMessageConstants.DisplayMessageActivationDate, "Activation date"),
    ;

    private final String key;
    private final String defaultFormat;

    DisplayDeviceMessageAttributes(String key, String defaultFormat) {
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