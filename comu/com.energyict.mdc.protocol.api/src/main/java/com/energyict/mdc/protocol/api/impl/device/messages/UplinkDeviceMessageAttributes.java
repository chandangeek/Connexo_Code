/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum UplinkDeviceMessageAttributes implements TranslationKey {

    enableUplinkPing(DeviceMessageConstants.enableUplinkPing, "Enable uplink ping"),
    uplinkPingDestinationAddress(DeviceMessageConstants.uplinkPingDestinationAddress, "Uplink ping destination dddress"),
    uplinkPingInterval(DeviceMessageConstants.uplinkPingInterval, "Uplink ping interval"),
    uplinkPingTimeout(DeviceMessageConstants.uplinkPingTimeout, "Uplink ping timeout"),
    ;

    private final String key;
    private final String defaultFormat;

    UplinkDeviceMessageAttributes(String key, String defaultFormat) {
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