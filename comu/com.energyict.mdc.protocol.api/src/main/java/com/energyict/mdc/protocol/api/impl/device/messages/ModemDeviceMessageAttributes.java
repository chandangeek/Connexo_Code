/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum ModemDeviceMessageAttributes implements TranslationKey {

    SetDialCommandAttributeName(DeviceMessageConstants.SetDialCommandAttributeName, "Set dial command"),
    SetModemInit1AttributeName(DeviceMessageConstants.SetModemInit1AttributeName, "Set modem init1"),
    SetModemInit2AttributeName(DeviceMessageConstants.SetModemInit2AttributeName, "Set modem init2"),
    SetModemInit3AttributeName(DeviceMessageConstants.SetModemInit3AttributeName, "Set modem init3"),
    SetPPPBaudRateAttributeName(DeviceMessageConstants.SetPPPBaudRateAttributeName, "Set PPP baudrate"),
    SetModemtypeAttributeName(DeviceMessageConstants.SetModemtypeAttributeName, "Set modem type"),
    SetResetCycleAttributeName(DeviceMessageConstants.SetResetCycleAttributeName, "Set reset cycle"),
    ;

    private final String key;
    private final String defaultFormat;

    ModemDeviceMessageAttributes(String key, String defaultFormat) {
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