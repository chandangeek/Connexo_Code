/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum PublicLightingDeviceMessageAttributes implements TranslationKey {

    relayNumberAttributeName(DeviceMessageConstants.relayNumberAttributeName, "relay number"),
    relayOperatingModeAttributeName(DeviceMessageConstants.relayOperatingModeAttributeName, "relay operating mode"),
    threshold(DeviceMessageConstants.threshold, "Threshold"),
    beginDatesAttributeName(DeviceMessageConstants.beginDatesAttributeName, "BeginDates"),
    endDatesAttributeName(DeviceMessageConstants.endDatesAttributeName, "EndDates"),
    offOffsetsAttributeName(DeviceMessageConstants.offOffsetsAttributeName, "OffOffsets"),
    onOffsetsAttributeName(DeviceMessageConstants.onOffsetsAttributeName, "OnOffsets"),
    latitudeAttributeName(DeviceMessageConstants.latitudeAttributeName, "latitude"),
    longitudeAttributeName(DeviceMessageConstants.longitudeAttributeName, "longitude"),
    ;

    private final String key;
    private final String defaultFormat;

    PublicLightingDeviceMessageAttributes(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return "PublicLighting." + key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}