package com.energyict.mdc.firmware;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides Firmware related services
 */
    public interface FirmwareService extends ReferencePropertySpecFinderProvider {

    public static String COMPONENTNAME = "FWC";
    
    public final int MAX_FIRMWARE_FILE_SIZE = 50 * 1024;

    /**
     * Provides a set of ProtocolSupportedFirmwareOptions for the given DeviceType
     */
    Set<ProtocolSupportedFirmwareOptions> getFirmwareOptionsFor(DeviceType deviceType);

    Query<? extends FirmwareVersion> getFirmwareVersionQuery();

    Finder<FirmwareVersion> findAllFirmwareVersions(Condition condition);

    Optional<FirmwareVersion> getFirmwareVersionById(long id);

    FirmwareVersion newFirmwareVersion(DeviceType deviceType, String firmwareVersion, FirmwareStatus status, FirmwareType type);

    void saveFirmwareVersion(FirmwareVersion firmwareVersion);

    void deprecateFirmwareVersion(FirmwareVersion firmwareVersion);

    boolean isFirmwareVersionInUse(long firmwareVersionId);

    Optional<FirmwareUpgradeOptions> findFirmwareUpgradeOptionsByDeviceType(DeviceType deviceType);

    /**
     * Provides a list of all <i>upgradable</i> FirmwareVersions for the given Device.
     * Depending on the FirmwareStatus of the FirmwareVersion and the DeviceLifeCycleStatus of the Device a filtered list will be provided.
     * If no FirmwareVersions are available, an empty list will be returned.
     *
     * @param device the device which requests the FirmwareVersions
     * @return a list of FirmwareVersions
     */
    List<FirmwareVersion> getAllUpgradableFirmwareVersionsFor(Device device);
}
