/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.energyict.mdc.firmware.SecurityAccessorOnDeviceType;
import com.energyict.mdc.firmware.rest.impl.campaign.DeviceInFirmwareCampaignInfo;
import com.energyict.mdc.firmware.rest.impl.campaign.DeviceInFirmwareCampaignInfoFactory;
import com.energyict.mdc.firmware.rest.impl.campaign.FirmwareCampaignInfo;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceHelper {
    private static final String FILTER_STATUS_PARAMETER = "firmwareStatus";
    private static final String FILTER_TYPE_PARAMETER = "firmwareType";

    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceService deviceService;
    private final FirmwareService firmwareService;
    private final FirmwareCampaignService firmwareCampaignService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final Thesaurus thesaurus;
    private final SecurityManagementService securityManagementService;
    private final Clock clock;
    private final DeviceInFirmwareCampaignInfoFactory deviceInFirmwareCampaignInfoFactory;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService,
                          DeviceMessageSpecificationService deviceMessageSpecificationService, DeviceService deviceService,
                          FirmwareService firmwareService, ConcurrentModificationExceptionFactory conflictFactory, Thesaurus thesaurus,
                          SecurityManagementService securityManagementService, Clock clock, DeviceInFirmwareCampaignInfoFactory deviceInFirmwareCampaignInfoFactory) {
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceService = deviceService;
        this.firmwareService = firmwareService;
        this.conflictFactory = conflictFactory;
        this.thesaurus = thesaurus;
        this.securityManagementService = securityManagementService;
        this.clock = clock;
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
        this.deviceInFirmwareCampaignInfoFactory = deviceInFirmwareCampaignInfoFactory;
    }

    public DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId));
    }

    public DeviceType findAndLockDeviceTypeOrThrowException(long deviceTypeId) {
        return deviceConfigurationService.findAndLockDeviceType(deviceTypeId)
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
        return lockFirmwareVersionOrThrowException(info.id, info.version, info.firmwareVersion);
    }

    public FirmwareVersion lockFirmwareVersionOrThrowException(long id, long version, String fwVersion) {
        return getLockedFirmwareVersionById(id, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(fwVersion)
                        .withActualVersion(() -> getCurrentFirmwareVersionVersion(id))
                        .supplier());
    }

    public Long getCurrentFirmwareCampaignVersion(long id) {
        return firmwareCampaignService.getFirmwareCampaignById(id).map(FirmwareCampaign::getVersion).orElse(null);
    }

    public Optional<FirmwareCampaign> getLockedFirmwareCampaign(long id, long version) {
        return firmwareCampaignService.findAndLockFirmwareCampaignByIdAndVersion(id, version);
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
    public DeviceMessageId findFirmwareMessageIdOrThrowException(DeviceType deviceType, String uploadOption, FirmwareVersion firmwareVersion) {
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

    public Optional<DeviceInFirmwareCampaignInfo> cancelDeviceInFirmwareCampaign(Device device, long campaignId) {
        Optional<DeviceInFirmwareCampaign> deviceInFirmwareCampaign = firmwareCampaignService.findActiveFirmwareItemByDevice(device);
        if (deviceInFirmwareCampaign.isPresent() && deviceInFirmwareCampaign.get().getParent().getId() == campaignId) {
            deviceInFirmwareCampaign.get().cancel();
            return Optional.of(deviceInFirmwareCampaignInfoFactory.createInfo(deviceInFirmwareCampaign.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<DeviceInFirmwareCampaignInfo> retryDeviceInFirmwareCampaign(Device device, long campaignId) {
        Optional<DeviceInFirmwareCampaign> deviceInFirmwareCampaign = firmwareCampaignService.findActiveFirmwareItemByDevice(device);
        if (deviceInFirmwareCampaign.isPresent() && deviceInFirmwareCampaign.get().getParent().getId() == campaignId) {
            deviceInFirmwareCampaign.get().retry();
            return Optional.of(deviceInFirmwareCampaignInfoFactory.createInfo(deviceInFirmwareCampaign.get()));
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
        return getCertificatesWithFileOperations().stream().filter(sa -> sa.getSecurityAccessorType().getId() == id).findAny();
    }

    public void deleteSecurityAccessorForSignatureValidation(long deviceTypeId) {
        Optional<DeviceType> deviceType = deviceConfigurationService.findDeviceType(deviceTypeId);
        Optional<SecurityAccessor> securityAccessor = findSecurityAccessorForSignatureValidation(deviceTypeId);
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

    public void checkFirmwareVersion(DeviceType deviceType, SecurityAccessor securityAccessor, byte[] firmwareFile) {
        if (securityAccessor.getActualValue().isPresent()
                && ((CertificateWrapper) securityAccessor.getActualValue().get()).getExpirationTime().filter(e -> e.isBefore(clock.instant())).isPresent()) {
            throw new LocalizedFieldValidationException(MessageSeeds.SECURITY_ACCESSOR_EXPIRED, "firmwareFile");
        }
        if (deviceType.getDeviceProtocolPluggableClass().filter(p -> p.getDeviceProtocol().firmwareSignatureCheckSupported()).isPresent()) {
            File tempFirmwareFile = null;
            try {
                tempFirmwareFile = File.createTempFile("tempFirmwareFile" + Instant.now().toEpochMilli(), ".tmp");
                try (FileOutputStream fos = new FileOutputStream(tempFirmwareFile)) {
                    fos.write(firmwareFile);
                }
                firmwareService.validateFirmwareFileSignature(deviceType, securityAccessor, tempFirmwareFile);
            } catch (Exception e) {
                throw new LocalizedFieldValidationException(MessageSeeds.SIGNATURE_VALIDATION_FAILED, "firmwareFile");
            } finally {
                if (tempFirmwareFile != null && tempFirmwareFile.exists()) {
                    tempFirmwareFile.delete();
                }
            }
        }
    }

    public Optional<Long> getPropertyInfoValueLong(PropertyInfo propertyInfo) {
        Object value = propertyInfo.getPropertyValueInfo().getValue();
        return this.convertObjectToLong(value);
    }

    private Optional<Long> convertObjectToLong(Object object) {
        if (object == null) {
            return Optional.empty();
        }
        String str = String.valueOf(object);
        return Optional.of(Long.parseLong(str));
    }

    public FirmwareVersionFilter getFirmwareFilter(JsonQueryFilter filter, DeviceType deviceType) {
        FirmwareVersionFilter firmwareVersionFilter = firmwareService.filterForFirmwareVersion(deviceType);

        if (filter.hasFilters()) {
            if (filter.hasProperty(FILTER_STATUS_PARAMETER)) {
                List<String> stringFirmwareStatuses = filter.getStringList(FILTER_STATUS_PARAMETER);
                List<FirmwareStatus> firmwareStatuses = stringFirmwareStatuses.stream().map(FirmwareStatusFieldAdapter.INSTANCE::unmarshal).collect(Collectors.toList());
                if (!firmwareStatuses.isEmpty()) {
                    firmwareVersionFilter.addFirmwareStatuses(firmwareStatuses);
                }
            }
            if (filter.hasProperty(FILTER_TYPE_PARAMETER)) {
                List<String> stringFirmwareTypes = filter.getStringList(FILTER_TYPE_PARAMETER);
                List<FirmwareType> firmwareTypes = stringFirmwareTypes.stream().map(FirmwareTypeFieldAdapter.INSTANCE::unmarshal).collect(Collectors.toList());
                if (!firmwareTypes.isEmpty()) {
                    firmwareVersionFilter.addFirmwareTypes(firmwareTypes);
                }
            }
        }
        return firmwareVersionFilter;
    }
}
