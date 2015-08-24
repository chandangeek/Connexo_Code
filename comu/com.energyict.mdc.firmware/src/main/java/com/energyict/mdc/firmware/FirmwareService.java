package com.energyict.mdc.firmware;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides Firmware related services.
 */
public interface FirmwareService extends ReferencePropertySpecFinderProvider {
    String COMPONENTNAME = "FWC";
    int MAX_FIRMWARE_FILE_SIZE = 50 * 1024 * 1024;

    // Firmware versions on a device type
    Finder<FirmwareVersion> findAllFirmwareVersions(FirmwareVersionFilter filter);
    Optional<FirmwareVersion> getFirmwareVersionById(long id);
    Optional<FirmwareVersion> getFirmwareVersionByVersionAndType(String version, FirmwareType firmwareType, DeviceType deviceType);
    FirmwareVersion newFirmwareVersion(DeviceType deviceType, String firmwareVersion, FirmwareStatus status, FirmwareType type);
    boolean isFirmwareVersionInUse(long firmwareVersionId);


    // Firmware upgrade options on a device type
    /**
     * Provides a set of ProtocolSupportedFirmwareOptions for the given DeviceType
     */
    Set<ProtocolSupportedFirmwareOptions> getSupportedFirmwareOptionsFor(DeviceType deviceType);
    Set<ProtocolSupportedFirmwareOptions> getAllowedFirmwareManagementOptionsFor(DeviceType deviceType);
    FirmwareManagementOptions newFirmwareManagementOptions(DeviceType deviceType);
    Optional<FirmwareManagementOptions> getFirmwareManagementOptions(DeviceType deviceType);
    Optional<FirmwareManagementOptions> findFirmwareManagementOptionsByDeviceType(DeviceType deviceType);
    List<DeviceType> getDeviceTypesWhichSupportFirmwareManagement();


    // Firmware versions on a device
    /**
     * Provides a list of all <i>upgradable</i> FirmwareVersions for the given Device.
     * Depending on the FirmwareStatus of the FirmwareVersion and the DeviceLifeCycleStatus of the Device a filtered list will be provided.
     * If no FirmwareVersions are available, an empty list will be returned.
     *
     * @param device the device which requests the FirmwareVersions
     * @param firmwareType which was requested for upgrade
     * @return a list of FirmwareVersions
     */
    List<FirmwareVersion> getAllUpgradableFirmwareVersionsFor(Device device, FirmwareType firmwareType);
    Optional<ActivatedFirmwareVersion> getActiveFirmwareVersion(Device device, FirmwareType firmwareType);
    ActivatedFirmwareVersion newActivatedFirmwareVersionFrom(Device device, FirmwareVersion firmwareVersion, Interval interval);
    PassiveFirmwareVersion newPassiveFirmwareVersionFrom(Device device, FirmwareVersion firmwareVersion, Interval interval);


    // Firmware campaigns
    Optional<FirmwareCampaign> getFirmwareCampaignById(long id);
    Finder<FirmwareCampaign> getFirmwareCampaigns();
    FirmwareCampaign newFirmwareCampaign(DeviceType deviceType, EndDeviceGroup endDeviceGroup);
    Finder<DeviceInFirmwareCampaign> getDevicesForFirmwareCampaign(FirmwareCampaign firmwareCampaign);
    void cancelFirmwareCampaign(FirmwareCampaign firmwareCampaign);

    /**
     * Tries to cancel the current FirmwareComTaskExecution on the device if it was still pending.
     *
     * @param device the device to cancel the firmware upload
     * @return true if we did a cancel, false if no action was required
     */
    boolean cancelFirmwareUploadForDevice(Device device);
    Optional<DeviceInFirmwareCampaign> getDeviceInFirmwareCampaignsForDevice(FirmwareCampaign firmwareCampaign, Device device);
}
