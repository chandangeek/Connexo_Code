package com.energyict.mdc.firmware;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;
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
    
    public final int MAX_FIRMWARE_FILE_SIZE = 50 * 1024 * 1024;

    // Firmware versions on a device type

    Query<? extends FirmwareVersion> getFirmwareVersionQuery();

    Finder<FirmwareVersion> findAllFirmwareVersions(Condition condition);

    Optional<FirmwareVersion> getFirmwareVersionById(long id);

    Optional<FirmwareVersion> getFirmwareVersionByVersion(String version, DeviceType deviceType);

    FirmwareVersion newFirmwareVersion(DeviceType deviceType, String firmwareVersion, FirmwareStatus status, FirmwareType type);

    void saveFirmwareVersion(FirmwareVersion firmwareVersion);

    void deprecateFirmwareVersion(FirmwareVersion firmwareVersion);

    boolean isFirmwareVersionInUse(long firmwareVersionId);

    // Firmware upgrade options on a device type

    /**
     * Provides a set of ProtocolSupportedFirmwareOptions for the given DeviceType
     */
    Set<ProtocolSupportedFirmwareOptions> getSupportedFirmwareOptionsFor(DeviceType deviceType);

    Set<ProtocolSupportedFirmwareOptions> getAllowedFirmwareUpgradeOptionsFor(DeviceType deviceType);

    FirmwareUpgradeOptions getFirmwareUpgradeOptions(DeviceType deviceType);

    void saveFirmwareUpgradeOptions(FirmwareUpgradeOptions firmwareOptions);

    // Firmware versions on a device

    /**
     * Provides a list of all <i>upgradable</i> FirmwareVersions for the given Device.
     * Depending on the FirmwareStatus of the FirmwareVersion and the DeviceLifeCycleStatus of the Device a filtered list will be provided.
     * If no FirmwareVersions are available, an empty list will be returned.
     *
     * @param device the device which requests the FirmwareVersions
     * @return a list of FirmwareVersions
     */
    List<FirmwareVersion> getAllUpgradableFirmwareVersionsFor(Device device);

    Optional<ActivatedFirmwareVersion> getCurrentMeterFirmwareVersionFor(Device device);

    Optional<ActivatedFirmwareVersion> getCurrentCommunicationFirmwareVersionFor(Device device);

    ActivatedFirmwareVersion newActivatedFirmwareVersionFrom(Device device, FirmwareVersion firmwareVersion, Interval interval);

    void saveActivatedFirmwareVersion(ActivatedFirmwareVersion activatedFirmwareVersion);

    PassiveFirmwareVersion newPassiveFirmwareVersionFrom(Device device, FirmwareVersion firmwareVersion, Interval interval);

    void savePassiveFirmwareVersion(PassiveFirmwareVersion passiveFirmwareVersion);

    RefAny findFirmwareUpgradeOptionsByDeviceType(DeviceType deviceType);
}
