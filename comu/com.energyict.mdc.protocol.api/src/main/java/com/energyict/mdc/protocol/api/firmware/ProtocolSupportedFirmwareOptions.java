package com.energyict.mdc.protocol.api.firmware;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

/**
 * Provides a plain summary of all FirmwareUpgrade options which a DeviceProtocol can support
 */
@ProviderType
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
        Optional<ProtocolSupportedFirmwareOptions> optional = Optional.empty();
        switch (id) {
            case "install":
                optional = Optional.of(UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
                break;
            case "activate":
                optional = Optional.of(UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
                break;
            case "activateOnDate":
                optional = Optional.of(UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
                break;
        }
        return optional;
    }
}
