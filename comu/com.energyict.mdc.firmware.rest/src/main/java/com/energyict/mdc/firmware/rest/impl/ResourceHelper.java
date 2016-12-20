package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;

public class ResourceHelper {
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceService deviceService;
    private final FirmwareService firmwareService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final Thesaurus thesaurus;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService, DeviceMessageSpecificationService deviceMessageSpecificationService, DeviceService deviceService, FirmwareService firmwareService, ConcurrentModificationExceptionFactory conflictFactory, Thesaurus thesaurus) {
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceService = deviceService;
        this.firmwareService = firmwareService;
        this.conflictFactory = conflictFactory;
        this.thesaurus = thesaurus;
    }

    public DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId));
    }

    public Device findDeviceByMridOrThrowException(String mRID) {
        return deviceService.findByUniqueMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEVICE_NOT_FOUND, mRID));
    }

    public Optional<Device> getLockedDevice(String mRID, long version) {
        return deviceService.findAndLockDeviceBymRIDAndVersion(mRID, version);
    }

    public FirmwareVersion findFirmwareVersionByIdOrThrowException(Long id) {
        return firmwareService.getFirmwareVersionById(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_VERSION_NOT_FOUND, id));
    }

    public Long getCurrentFirmwareVersionVersion(long id) {
        return firmwareService.getFirmwareVersionById(id).map(FirmwareVersion::getVersion).orElse(null);
    }

    public Optional<FirmwareVersion> getLockedFirmwareVersionById(long id, long version) {
        return firmwareService.findAndLockFirmwareVersionByIdAndVersion(id, version);
    }

    public FirmwareVersion lockFirmwareVersionOrThrowException(FirmwareVersionInfo info) {
        return getLockedFirmwareVersionById(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.firmwareVersion)
                        .withActualVersion(() -> getCurrentFirmwareVersionVersion(info.id))
                        .supplier());
    }

    public FirmwareCampaign findFirmwareCampaignOrThrowException(long id) {
        return firmwareService.getFirmwareCampaignById(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_CAMPAIGN_NOT_FOUND, id));
    }

    public Long getCurrentFirmwareCampaignVersion(long id) {
        return firmwareService.getFirmwareCampaignById(id).map(FirmwareCampaign::getVersion).orElse(null);
    }

    public Optional<FirmwareCampaign> getLockedFirmwareCampaign(long id, long version) {
        return firmwareService.findAndLockFirmwareCampaignByIdAndVersion(id, version);
    }

    public FirmwareCampaign lockFirmwareCampaign(FirmwareCampaignInfo info) {
        return getLockedFirmwareCampaign(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentFirmwareCampaignVersion(info.id))
                        .supplier());
    }

    public ProtocolSupportedFirmwareOptions findProtocolSupportedFirmwareOptionsOrThrowException(String uploadOption) {
        return ProtocolSupportedFirmwareOptions.from(uploadOption)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
    }

    /**
     * Returns the appropriate DeviceMessageId which corresponds with the uploadOption
     */
    public DeviceMessageId findFirmwareMessageIdOrThrowException(DeviceType deviceType, String firmwareOption) {
        ProtocolSupportedFirmwareOptions targetFirmwareOptions = findProtocolSupportedFirmwareOptionsOrThrowException(firmwareOption);
        return deviceType.getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages()).orElse(Collections.emptyList())
                .stream()
                .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getMessageId)
                .map(DeviceMessageId::havingId)
                .filter(firmwareMessageCandidate -> {
                    Optional<ProtocolSupportedFirmwareOptions> firmwareOptionForCandidate = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(firmwareMessageCandidate);
                    return firmwareOptionForCandidate.isPresent() && targetFirmwareOptions.equals(firmwareOptionForCandidate.get());
                })
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
    }

    public DeviceMessageSpec findFirmwareMessageSpecOrThrowException(DeviceType deviceType, String firmwareOption) {
        DeviceMessageId firmwareMessageId = findFirmwareMessageIdOrThrowException(deviceType, firmwareOption);
        return this.findFirmwareMessageSpecOrThrowException(firmwareMessageId);
    }

    public DeviceMessageSpec findFirmwareMessageSpecOrThrowException(DeviceMessageId firmwareMessageId) {
        return deviceMessageSpecificationService.findMessageSpecById(firmwareMessageId.dbValue())
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
    }

    public Optional<DeviceInFirmwareCampaignInfo> cancelDeviceInFirmwareCampaign(FirmwareCampaign campaign, Device device) {
        Optional<DeviceInFirmwareCampaign> deviceInFirmwareCampaign = firmwareService.getDeviceInFirmwareCampaignsForDevice(campaign, device);
        if (deviceInFirmwareCampaign.isPresent()) {
            firmwareService.cancelFirmwareUploadForDevice(device);
            deviceInFirmwareCampaign.get().cancel();
            return Optional.of(new DeviceInFirmwareCampaignInfo(deviceInFirmwareCampaign.get(),thesaurus));
        } else {
            return Optional.empty();
        }
    }

    public Optional<DeviceInFirmwareCampaignInfo> retryDeviceInFirmwareCampaign(FirmwareCampaign campaign, Device device) {
        Optional<DeviceInFirmwareCampaign> deviceInFirmwareCampaign = firmwareService.getDeviceInFirmwareCampaignsForDevice(campaign, device);
        if (deviceInFirmwareCampaign.isPresent()) {
            firmwareService.retryFirmwareUploadForDevice(deviceInFirmwareCampaign.get());
            deviceInFirmwareCampaign.get().retry();
            return Optional.of(new DeviceInFirmwareCampaignInfo(deviceInFirmwareCampaign.get(),thesaurus));
        } else {
            return Optional.empty();
        }
    }

}
