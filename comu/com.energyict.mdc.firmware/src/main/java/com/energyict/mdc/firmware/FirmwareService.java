package com.energyict.mdc.firmware;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import java.util.Optional;
import java.util.Set;

/**
 * Provides Firmware related services
 */
public interface FirmwareService {

    public static String COMPONENTNAME = "FWC";
    
    public final int MAX_FIRMWARE_FILE_SIZE = 50 * 1024;

    /**
     * Provides a set of ProtocolSupportedFirmwareOptions for the given DeviceType
     */
    Set<ProtocolSupportedFirmwareOptions> getFirmwareOptionsFor(DeviceType deviceType);

    Query<? extends FirmwareVersion> getFirmwareVersionQuery();

    Finder<FirmwareVersion> findAllFirmwareVersions(Condition condition);

    Optional<FirmwareVersion> getFirmwareVersionById(long id);

    void saveFirmwareVersion(FirmwareVersion firmwareVersion);

    void deprecateFirmwareVersion(FirmwareVersion firmwareVersion);

    Optional<FirmwareUpgradeOptions> findFirmwareUpgradeOptionsByDeviceType(DeviceType deviceTypeId);

}
