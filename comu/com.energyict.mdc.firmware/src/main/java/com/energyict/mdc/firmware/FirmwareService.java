package com.energyict.mdc.firmware;


import java.util.Set;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceType;

/**
 * Provides Firmware related services
 */
public interface FirmwareService {

    public static String COMPONENT_NAME = "FWC";
    
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
