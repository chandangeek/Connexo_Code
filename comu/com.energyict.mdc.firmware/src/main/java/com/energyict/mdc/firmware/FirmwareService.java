package com.energyict.mdc.firmware;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceType;

import java.util.Optional;

public interface FirmwareService {
    String COMPONENTNAME = "FWC";

    final int MAX_FIRMWARE_FILE_SIZE = 50 * 1024;

    Query<? extends FirmwareVersion> getFirmwareVersionQuery();

    Finder<FirmwareVersion> findAllFirmwareVersions(Condition condition);

    Optional<FirmwareVersion> getFirmwareVersionById(long id);

    void saveFirmwareVersion(FirmwareVersion firmwareVersion);

    void deprecateFirmwareVersion(FirmwareVersion firmwareVersion);

    Optional<FirmwareUpgradeOptions> findFirmwareUpgradeOptionsByDeviceType(DeviceType deviceTypeId);

    //Set<ProtocolSupportedFirmwareOptions> getFirmwareOptionsFor(DeviceType deviceType);
}
