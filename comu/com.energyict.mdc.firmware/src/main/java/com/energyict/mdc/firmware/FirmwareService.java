package com.energyict.mdc.firmware;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import java.util.Set;

/**
 * Provides Firmware related services
 */
public interface FirmwareService {

    public static String COMPONENT_NAME = "FWC";

    /**
     * Provides a set of ProtocolSupportedFirmwareOptions for the given DeviceType
     */
    Set<ProtocolSupportedFirmwareOptions> getFirmwareOptionsFor(DeviceType deviceType);

}
