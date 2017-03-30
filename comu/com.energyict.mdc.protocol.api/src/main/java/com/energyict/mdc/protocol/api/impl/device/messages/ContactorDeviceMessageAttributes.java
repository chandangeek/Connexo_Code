/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum ContactorDeviceMessageAttributes implements TranslationKey {

    contactorActivationDateAttributeName(DeviceMessageConstants.contactorActivationDateAttributeName, "Activation date"),
    digitalOutputAttributeName(DeviceMessageConstants.digitalOutputAttributeName, "Digital output"),
    contactorModeAttributeName(DeviceMessageConstants.contactorModeAttributeName, "Changemode mode"),
    relayNumberAttributeName(DeviceMessageConstants.relayNumberAttributeName, "Relay number"),
    relayOperatingModeAttributeName(DeviceMessageConstants.relayOperatingModeAttributeName, "Relay operating mode"),
    ;

    private final String key;
    private final String defaultFormat;

    ContactorDeviceMessageAttributes(String key, String defaultFormat) {
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