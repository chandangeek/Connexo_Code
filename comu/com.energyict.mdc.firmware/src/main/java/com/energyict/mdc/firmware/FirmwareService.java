/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import aQute.bnd.annotation.ProviderType;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Provides Firmware related services.
 */
@ProviderType
public interface FirmwareService {
    String COMPONENTNAME = "FWC";
    int MAX_FIRMWARE_FILE_SIZE = 150 * 1024 * 1024;

    // Firmware versions on a device type

    FirmwareVersionFilter filterForFirmwareVersion(DeviceType deviceType);

    Finder<FirmwareVersion> findAllFirmwareVersions(FirmwareVersionFilter filter);

    Optional<FirmwareVersion> getFirmwareVersionById(long id);

    Optional<FirmwareVersion> findAndLockFirmwareVersionByIdAndVersion(long id, long version);

    Optional<FirmwareVersion> getFirmwareVersionByVersionAndType(String version, FirmwareType firmwareType, DeviceType deviceType);

    FirmwareVersionBuilder newFirmwareVersion(DeviceType deviceType, String firmwareVersion, FirmwareStatus status, FirmwareType type);

    FirmwareVersionBuilder newFirmwareVersion(DeviceType deviceType, String firmwareVersion, FirmwareStatus status, FirmwareType type, String imageIdentifier);

    boolean isFirmwareVersionInUse(long firmwareVersionId);

    Finder<SecurityAccessorOnDeviceType> findSecurityAccessorForSignatureValidation(DeviceType deviceType, SecurityAccessor securityAccessor);

    Finder<SecurityAccessorOnDeviceType> findSecurityAccessorForSignatureValidation(SecurityAccessor securityAccessor);

    Finder<SecurityAccessorOnDeviceType> findSecurityAccessorForSignatureValidation(DeviceType deviceType);

    void addSecurityAccessorForSignatureValidation(DeviceType deviceType, SecurityAccessor securityAccessor);

    void deleteSecurityAccessorForSignatureValidation(DeviceType deviceType, SecurityAccessor securityAccessor);

    void validateFirmwareFileSignature(DeviceType deviceType, SecurityAccessor securityAccessor, File firmwareFile);

    Thesaurus getThesaurus();


    // Firmware upgrade options on a device type

    /**
     * Provides a set of ProtocolSupportedFirmwareOptions for the given DeviceType
     */
    Set<ProtocolSupportedFirmwareOptions> getSupportedFirmwareOptionsFor(DeviceType deviceType);

    EnumSet<FirmwareType> getSupportedFirmwareTypes(DeviceType deviceType);

    boolean imageIdentifierExpectedAtFirmwareUpload(DeviceType deviceType);

    boolean isResumeFirmwareUploadEnabled(DeviceType deviceType);

    Optional<DeviceMessageSpec> defaultFirmwareVersionSpec();

    Optional<DeviceMessageId> bestSuitableFirmwareUpgradeMessageId(DeviceType deviceType, ProtocolSupportedFirmwareOptions firmwareManagementOption, FirmwareVersion firmwareVersion);

    Set<ProtocolSupportedFirmwareOptions> getAllowedFirmwareManagementOptionsFor(DeviceType deviceType);

    FirmwareManagementOptions newFirmwareManagementOptions(DeviceType deviceType);

    Optional<FirmwareManagementOptions> findFirmwareManagementOptions(DeviceType deviceType);

    Optional<FirmwareManagementOptions> findAndLockFirmwareManagementOptionsByIdAndVersion(DeviceType deviceType, long version);

    List<DeviceType> getDeviceTypesWhichSupportFirmwareManagement();


    // Firmware versions on a device

    /**
     * Provides a list of all <i>upgradable</i> FirmwareVersions for the given Device.
     * Depending on the FirmwareStatus of the FirmwareVersion and the DeviceLifeCycleStatus of the Device a filtered list will be provided.
     * If no FirmwareVersions are available, an empty list will be returned.
     *
     * @param device       the device which requests the FirmwareVersions
     * @param firmwareType which was requested for upgrade
     * @return a list of FirmwareVersions
     */
    List<FirmwareVersion> getAllUpgradableFirmwareVersionsFor(Device device, FirmwareType firmwareType);

    Optional<ActivatedFirmwareVersion> getActiveFirmwareVersion(Device device, FirmwareType firmwareType);

    ActivatedFirmwareVersion newActivatedFirmwareVersionFrom(Device device, FirmwareVersion firmwareVersion, Interval interval);

    PassiveFirmwareVersion newPassiveFirmwareVersionFrom(Device device, FirmwareVersion firmwareVersion, Interval interval);

    /**
     * Tries to cancel the current FirmwareComTaskExecution on the device if it was still pending.
     *
     * @param device the device to cancel the firmware upload
     * @return true if we did a cancel, false if no action was required
     */
    void cancelFirmwareUploadForDevice(Device device);

    /**
     * Tries to resume the current FirmwareComTaskExecution on the device if it was still pending.
     *
     * @param device the device to cancel the firmware upload
     */
    void resumeFirmwareUploadForDevice(Device device);

    /**
     * Gets a utility class to manage the firmware on a single device
     *
     * @param device the device to manage the firmware for
     * @return the utility class
     */
    FirmwareManagementDeviceUtils getFirmwareManagementDeviceUtilsFor(Device device);

    FirmwareManagementDeviceUtils getFirmwareManagementDeviceUtilsFor(Device device, boolean onlyLastMessagePerFirmwareType);

    DeviceFirmwareHistory getFirmwareHistory(Device device);

    Stream<FirmwareCheck> getFirmwareChecks();

    List<? extends FirmwareVersion> getOrderedFirmwareVersions(DeviceType deviceType);

    /**
     * @param kPermutation Should be obtained for the whole list of firmware versions on current deviceType, ordered by rank desc.
     */
    void reorderFirmwareVersions(DeviceType deviceType, KPermutation kPermutation);

    FirmwareCampaignService getFirmwareCampaignService();

    Optional<DeviceMessageSpec> getFirmwareMessageSpec(DeviceType deviceType, ProtocolSupportedFirmwareOptions firmwareManagementOptions,
                                                       FirmwareVersion firmwareVersion);

    FirmwareCampaignManagementOptions newFirmwareCampaignCheckManagementOptions(FirmwareCampaign firmwareCampaign);

    Optional<FirmwareCampaignManagementOptions> findFirmwareCampaignCheckManagementOptions(FirmwareCampaign firmwareCampaign);

    void createFirmwareCampaignVersionStateSnapshot(FirmwareCampaign firmwareCampaign, FirmwareVersion foundFirmware);

    List<FirmwareCampaignVersionStateShapshot> findFirmwareCampaignVersionStateSnapshots(FirmwareCampaign firmwareCampaign);

    String getLocalizedFirmwareStatus(FirmwareStatus firmwareStatus);

    boolean hasRunningFirmwareTask(Device device);
}
