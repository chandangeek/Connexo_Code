package com.energyict.mdc.protocol.api.device.messages;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Copyrights EnergyICT
 * Date: 30.04.15
 * Time: 15:35
 */
public enum DeviceMessageAttributes implements TranslationKey {

    firmwareUpdateFileAttributeName(DeviceMessageConstants.firmwareUpdateFileAttributeName, "Firmware version"),
    resumeFirmwareUpdateAttributeName(DeviceMessageConstants.resumeFirmwareUpdateAttributeName, "Resume firmware upload"),
    plcTypeFirmwareUpdateAttributeName(DeviceMessageConstants.plcTypeFirmwareUpdateAttributeName, "PLC type"),
    firmwareUpdateActivationDateAttributeName(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName, "Activation date"),
    firmwareUpdateVersionNumberAttributeName(DeviceMessageConstants.firmwareUpdateVersionNumberAttributeName, "Version number"),
    firmwareUpdateURLAttributeName(DeviceMessageConstants.firmwareUpdateURLAttributeName, "Download url"),
    ;

    private final String key;
    private final String defaultFormat;

    DeviceMessageAttributes(String key, String defaultFormat) {
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
