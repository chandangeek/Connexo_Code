/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum FirmwareDeviceMessageAttributes implements TranslationKey {

    firmwareUpdateActivationDateAttributeName(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName, "Upgrade activation date"),
    firmwareUpdateVersionNumberAttributeName(DeviceMessageConstants.firmwareUpdateVersionNumberAttributeName, "Upgrade version"),
    firmwareUpdateFileAttributeName(DeviceMessageConstants.firmwareUpdateFileAttributeName, "Upgrade firwareversion"),
    firmwareUpdateImageIdentifierAttributeName(DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName, "Image identifier"),
    resumeFirmwareUpdateAttributeName(DeviceMessageConstants.resumeFirmwareUpdateAttributeName, "Upgrade resume"),
    plcTypeFirmwareUpdateAttributeName(DeviceMessageConstants.plcTypeFirmwareUpdateAttributeName, "Upgrade plc"),
    firmwareUpdateURLAttributeName(DeviceMessageConstants.firmwareUpdateURLAttributeName, "Upgrade url"),
    ;

    private final String key;
    private final String defaultFormat;

    FirmwareDeviceMessageAttributes(String key, String defaultFormat) {
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