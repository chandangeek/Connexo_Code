/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum OpusDeviceMessageAttributes implements TranslationKey {

    SetOpusOSNbrAttributeName(DeviceMessageConstants.SetOpusOSNbrAttributeName, "Set Opus OSNbr"),
    SetOpusPasswordAttributeName(DeviceMessageConstants.SetOpusPasswordAttributeName, "Set Opus password"),
    SetOpusTimeoutAttributeName(DeviceMessageConstants.SetOpusTimeoutAttributeName, "Set Opus timeout"),
    SetOpusConfigAttributeName(DeviceMessageConstants.SetOpusConfigAttributeName, "Set Opus config"),
    ;

    private final String key;
    private final String defaultFormat;

    OpusDeviceMessageAttributes(String key, String defaultFormat) {
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