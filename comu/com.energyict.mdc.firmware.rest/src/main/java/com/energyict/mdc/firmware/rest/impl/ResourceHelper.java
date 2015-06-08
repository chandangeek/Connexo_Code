package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.inject.Inject;
import java.util.Optional;

public class ResourceHelper {
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceService deviceService;
    private final FirmwareService firmwareService;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService, DeviceMessageSpecificationService deviceMessageSpecificationService, DeviceService deviceService, FirmwareService firmwareService) {
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceService = deviceService;
        this.firmwareService = firmwareService;
    }

    public DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId));
    }

    public Device findDeviceByMridOrThrowException(String mRID) {
        return deviceService.findByUniqueMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEVICE_NOT_FOUND, mRID));
    }

    public FirmwareVersion findFirmwareVersionByIdOrThrowException(Long id) {
        return firmwareService.getFirmwareVersionById(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_VERSION_NOT_FOUND, id));
    }

    public FirmwareCampaign findFirmwareCampaignOrThrowException(long id){
        return firmwareService.getFirmwareCampaignById(id)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_CAMPAIGN_NOT_FOUND, id));
    }
    public ProtocolSupportedFirmwareOptions findProtocolSupportedFirmwareOptionsOrThrowException(String uploadOption) {
        return ProtocolSupportedFirmwareOptions.from(uploadOption)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
    }

    /** Returns the appropriate DeviceMessageId which corresponds with the uploadOption */
    public DeviceMessageId findFirmwareMessageIdOrThrowException(DeviceType deviceType, String firmwareOption) {
        ProtocolSupportedFirmwareOptions targetFirmwareOptions = findProtocolSupportedFirmwareOptionsOrThrowException(firmwareOption);
        return deviceType.getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages()
                .stream()
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
}
