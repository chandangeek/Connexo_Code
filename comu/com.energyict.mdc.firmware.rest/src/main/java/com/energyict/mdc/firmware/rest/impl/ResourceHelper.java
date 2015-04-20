package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Predicate;

public class ResourceHelper {
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final FirmwareService firmwareService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    private final DeviceMessageCategory firmwareCategory;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService, FirmwareService firmwareService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.firmwareService = firmwareService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;

        this.firmwareCategory = this.deviceMessageSpecificationService.getFirmwareCategory();
    }

    public DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId));
    }

    public Device findDeviceByMridOrThrowException(String mRID) {
        return deviceService.findByUniqueMrid(mRID)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_NOT_FOUND, mRID));
    }

    public FirmwareVersion findFirmwareVersionByIdOrThrowException(Long id) {
        return firmwareService.getFirmwareVersionById(id).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.FIRMWARE_VERSION_NOT_FOUND, id));
    }

    public Predicate<DeviceMessage<Device>> filterPendingMessagesPredicate(){
        return candidate -> candidate.getStatus().equals(DeviceMessageStatus.PENDING) || candidate.getStatus().equals(DeviceMessageStatus.WAITING);
    }

    public Predicate<DeviceMessage<Device>> filterFirmwareUpgradeMessagesPredicate(){
        return candidate -> candidate.getSpecification().getCategory().getId() == firmwareCategory.getId();
    }

    public Optional<FirmwareVersion> getFirmwareVersionFromMessage(DeviceMessage<Device> firmwareMessage){
        Optional<DeviceMessageAttribute> firmwareVersionMessageAttr = firmwareMessage.getAttributes().stream()
                .filter(attr -> DeviceMessageConstants.firmwareUpdateFileAttributeName.equals(attr.getName()))
                .findFirst();
        return firmwareVersionMessageAttr.isPresent() ?
                Optional.of((FirmwareVersion) firmwareVersionMessageAttr.get().getValue()):
                Optional.empty();
    }
}
