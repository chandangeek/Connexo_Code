package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;

public class ResourceHelper {
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final FirmwareService firmwareService;
    private final MeteringGroupsService meteringGroupsService;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService, FirmwareService firmwareService, MeteringGroupsService meteringGroupsService) {
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.firmwareService = firmwareService;
        this.meteringGroupsService = meteringGroupsService;
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
        return firmwareService.getFirmwareVersionById(id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.FIRMWARE_VERSION_NOT_FOUND, id));
    }

    public FirmwareCampaign findFirmwareCampaignOrThrowException(long id){
        return firmwareService.getFirmwareCampaignById(id)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.FIRMWARE_CAMPAIGN_NOT_FOUND, id));
    }

    public EndDeviceGroup findDeviceGroupOrThrowException(long id){
        return meteringGroupsService.findEndDeviceGroup(id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_GROUP_NOT_FOUND, id));
    }
}
