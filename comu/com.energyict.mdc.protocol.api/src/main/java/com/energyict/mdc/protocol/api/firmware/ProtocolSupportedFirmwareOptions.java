package com.energyict.mdc.protocol.api.firmware;

/**
 * Provides a plain summary of all FirmwareUpgrade options which a DeviceProtocol can support
 */
public enum ProtocolSupportedFirmwareOptions {

    UPLOAD_FIRMWARE_AND_ACTIVATE_LATER,
    UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE,
    UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE

}
