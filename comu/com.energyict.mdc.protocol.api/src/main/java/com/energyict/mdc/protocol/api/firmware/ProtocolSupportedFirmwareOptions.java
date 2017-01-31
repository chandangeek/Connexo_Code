/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.firmware;

import java.util.Arrays;
import java.util.Optional;

/**
 * Provides a plain summary of all FirmwareUpgrade options which a DeviceProtocol can support
 */
public enum ProtocolSupportedFirmwareOptions {

    UPLOAD_FIRMWARE_AND_ACTIVATE_LATER("install"),
    UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE("activate"),
    UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE("activateOnDate");

    private String id;

    ProtocolSupportedFirmwareOptions(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Optional<ProtocolSupportedFirmwareOptions> from(String id) {
        return Arrays.stream(values()).filter(option -> option.getId().equals(id)).findFirst();
    }
}
