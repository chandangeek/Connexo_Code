/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.rest.PropertyInfo;
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
import com.energyict.mdc.firmware.SecurityAccessorOnDeviceType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceHelper {
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceService deviceService;
    private final FirmwareService firmwareService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final Thesaurus thesaurus;
    private final SecurityManagementService securityManagementService;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService, DeviceMessageSpecificationService deviceMessageSpecificationService, DeviceService deviceService, FirmwareService firmwareService, ConcurrentModificationExceptionFactory conflictFactory, Thesaurus thesaurus, SecurityManagementService securityManagementService) {
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceService = deviceService;
        this.firmwareService = firmwareService;
        this.conflictFactory = conflictFactory;
        this.thesaurus = thesaurus;
        this.securityManagementService = securityManagementService;
    }

    public DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId));
    }

    public Device findDeviceByNameOrThrowException(String deviceName) {
        return deviceService.findDeviceByName(deviceName)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEVICE_NOT_FOUND, deviceName));
    }

    public Optional<Device> getLockedDevice(String deviceName, long version) {
        return deviceService.findAndLockDeviceByNameAndVersion(deviceName, version);
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
    public DeviceMessageId findFirmwareMessageIdOrThrowException(DeviceType deviceType, String uploadOption,  FirmwareVersion firmwareVersion) {
        return firmwareService.bestSuitableFirmwareUpgradeMessageId(deviceType, findProtocolSupportedFirmwareOptionsOrThrowException(uploadOption), firmwareVersion)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
    }

    public DeviceMessageSpec findFirmwareMessageSpecOrThrowException(DeviceType deviceType, String uploadOption) {
        FirmwareMessageInfo firmwareMessageInfo = new FirmwareMessageInfo();
        firmwareMessageInfo.uploadOption = uploadOption;
        return findFirmwareMessageSpecOrThrowException(deviceType, firmwareMessageInfo);
    }

    public DeviceMessageSpec findFirmwareMessageSpecOrThrowException(DeviceType deviceType, FirmwareMessageInfo messageInfo) {
        DeviceMessageId firmwareMessageId = findFirmwareMessageIdOrThrowException(deviceType, messageInfo.uploadOption, null);
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

    public List<SecurityAccessor> getCertificatesWithFileOperations() {
        return securityManagementService.getSecurityAccessors(SecurityAccessorType.Purpose.FILE_OPERATIONS)
                .stream()
                .filter(sa -> sa.getActualValue().isPresent() && sa.getActualValue().get() instanceof CertificateWrapper)
                .collect(Collectors.toList());
    }

    public Optional<SecurityAccessor> getCertificateWithFileOperations(long id) {
        return getCertificatesWithFileOperations().stream().filter(sa -> sa.getKeyAccessorType().getId() == id).findAny();
    }

   public void deleteSecurityAccessorForSignatureValidation(long securityAccessorId, long deviceTypeId) {
       Optional<DeviceType> deviceType = deviceConfigurationService.findDeviceType(deviceTypeId);
       Optional<SecurityAccessor> securityAccessor = getCertificateWithFileOperations(securityAccessorId);
       if (deviceType.isPresent() && securityAccessor.isPresent()) {
           firmwareService.deleteSecurityAccessorForSignatureValidation(deviceType.get(), securityAccessor.get());
       }
   }

    public void addSecurityAccessorForSignatureValidation(long securityAccessorId, long deviceTypeId) {
        Optional<DeviceType> deviceType = deviceConfigurationService.findDeviceType(deviceTypeId);
        Optional<SecurityAccessor> securityAccessor = getCertificateWithFileOperations(securityAccessorId);
        if (deviceType.isPresent() && securityAccessor.isPresent()) {
            firmwareService.addSecurityAccessorForSignatureValidation(deviceType.get(), securityAccessor.get());
        }
    }

    public Optional<SecurityAccessor> findSecurityAccessorForSignatureValidation(long deviceTypeId) {
        Optional<DeviceType> deviceType = deviceConfigurationService.findDeviceType(deviceTypeId);
        return deviceType.flatMap(dt -> firmwareService.findSecurityAccessorForSignatureValidation(dt).find().stream()
                .filter(securityAccessorOnDeviceType -> securityAccessorOnDeviceType.getDeviceType().getId() == deviceTypeId)
                .findAny()
                .map(SecurityAccessorOnDeviceType::getSecurityAccessor));
    }

    Optional<Long> getPropertyInfoValueLong(PropertyInfo propertyInfo) {
        Object value = propertyInfo.getPropertyValueInfo().getValue();
        return this.convertObjectToLong(value);
    }

    private Optional<Long> convertObjectToLong(Object object) {
        if (object == null){
            return Optional.empty();
        }
        String str = String.valueOf(object);
        return Optional.of(Long.parseLong(str));
    }


}
