package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

/**
 * Copyrights EnergyICT
 * Date: 30.04.15
 * Time: 15:35
 */
enum ChannelConfigurationDeviceMessageAttributes implements TranslationKey {

    ID("ChannelConfigurationDeviceMessage." + DeviceMessageConstants.id, "ID"),
    SetFunctionAttributeName(DeviceMessageConstants.SetFunctionAttributeName, "Set function"),
    SetParametersAttributeName(DeviceMessageConstants.SetParametersAttributeName, "Set parameters"),
    SetNameAttributeName(DeviceMessageConstants.SetNameAttributeName, "Set name"),
    SetUnitAttributeName(DeviceMessageConstants.SetUnitAttributeName, "Set unit"),
    ChannelConfigurationChnNbrAttributeName(DeviceMessageConstants.ChannelConfigurationChnNbrAttributeName, "Channel number"),
    DivisorAttributeName(DeviceMessageConstants.DivisorAttributeName, "Divisor"),
    ;

    private final String key;
    private final String defaultFormat;

    ChannelConfigurationDeviceMessageAttributes(String key, String defaultFormat) {
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