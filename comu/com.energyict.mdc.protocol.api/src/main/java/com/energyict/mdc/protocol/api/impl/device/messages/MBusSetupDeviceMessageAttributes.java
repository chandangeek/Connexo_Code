/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum MBusSetupDeviceMessageAttributes implements TranslationKey {

    openKeyAttributeName(DeviceMessageConstants.openKeyAttributeName, "Open key"),
    transferKeyAttributeName(DeviceMessageConstants.transferKeyAttributeName, "Transfer key"),
    defaultKeyAttributeName(DeviceMessageConstants.defaultKeyAttributeName, "Default key"),
    dib(DeviceMessageConstants.dib, "Dib"),
    vib(DeviceMessageConstants.vib, "Vib"),
    dibInstance1(DeviceMessageConstants.dibInstance1, "Dib instance 1"),
    vibInstance1(DeviceMessageConstants.vibInstance1, "Vib instance 1"),
    dibInstance2(DeviceMessageConstants.dibInstance2, "Dib instance 2"),
    vibInstance2(DeviceMessageConstants.vibInstance2, "Vib instance 2"),
    dibInstance3(DeviceMessageConstants.dibInstance3, "Dib instance 3"),
    vibInstance3(DeviceMessageConstants.vibInstance3, "Vib instance 3"),
    dibInstance4(DeviceMessageConstants.dibInstance4, "Dib instance 4"),
    vibInstance4(DeviceMessageConstants.vibInstance4, "Vib instance 4"),
    mbusChannel(DeviceMessageConstants.mbusChannel, "Channel"),
    ;

    private final String key;
    private final String defaultFormat;

    MBusSetupDeviceMessageAttributes(String key, String defaultFormat) {
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