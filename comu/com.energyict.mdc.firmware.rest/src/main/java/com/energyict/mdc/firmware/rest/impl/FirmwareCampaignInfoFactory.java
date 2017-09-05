/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Collectors;

public class FirmwareCampaignInfoFactory {
    private final Thesaurus thesaurus;
    private final ResourceHelper resourceHelper;
    private final FirmwareService firmwareService;
    private final MeteringGroupsService meteringGroupsService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final FirmwareVersionInfoFactory firmwareVersionFactory;
    private final FirmwareMessageInfoFactory firmwareMessageInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public FirmwareCampaignInfoFactory(
            Thesaurus thesaurus,
            FirmwareService firmwareService,
            MeteringGroupsService meteringGroupsService,
            DeviceConfigurationService deviceConfigurationService,
            DeviceMessageSpecificationService deviceMessageSpecificationService,
            ResourceHelper resourceHelper,
            FirmwareVersionInfoFactory firmwareVersionFactory,
            FirmwareMessageInfoFactory firmwareMessageInfoFactory, ExceptionFactory exceptionFactory) {
        this.thesaurus = thesaurus;
        this.resourceHelper = resourceHelper;
        this.firmwareService = firmwareService;
        this.meteringGroupsService = meteringGroupsService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.firmwareVersionFactory = firmwareVersionFactory;
        this.firmwareMessageInfoFactory = firmwareMessageInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    public FirmwareCampaignInfo from(FirmwareCampaign campaign){
        FirmwareCampaignInfo info = new FirmwareCampaignInfo();
        info.id = campaign.getId();
        info.name = campaign.getName();
        FirmwareCampaignStatus campaignStatus = campaign.getStatus();
        info.status = new IdWithLocalizedValue<>();
        info.status.id = campaignStatus.name();
        String statusTranslationKey = new FirmwareCampaignStatusAdapter().marshal(campaignStatus);
        info.status.localizedValue = thesaurus.getString(statusTranslationKey, statusTranslationKey);
        info.deviceType = IdWithLocalizedValue.from(campaign.getDeviceType());
        String managementOptionId = campaign.getFirmwareManagementOption().getId();
        info.managementOption = new ManagementOptionInfo(managementOptionId, thesaurus.getString(managementOptionId, managementOptionId));
        info.firmwareType = new FirmwareTypeInfo(campaign.getFirmwareType(), thesaurus);
        info.startedOn = campaign.getStartedOn();
        info.finishedOn = campaign.getFinishedOn();
        if (campaign.getComWindow() != null) {
            info.timeBoundaryStart = campaign.getComWindow().getStart().getMillis() / 1000;
            info.timeBoundaryEnd = campaign.getComWindow().getEnd().getMillis() / 1000;
        }
        Optional<DeviceMessageSpec> firmwareMessageSpec = campaign.getFirmwareMessageSpec();
        if (firmwareMessageSpec.isPresent()) {
            info.firmwareVersion = firmwareVersionFactory.from(campaign.getFirmwareVersion());
            info.properties = firmwareMessageInfoFactory.getProperties(firmwareMessageSpec.get(),campaign.getDeviceType(), info.firmwareType.id.getType(), campaign.getProperties());
        }
        info.devicesStatus = campaign.getDevicesStatusMap().entrySet()
                .stream()
                .map(status -> new DeviceInFirmwareCampaignStatusInfo(status.getKey(), status.getValue(), thesaurus))
                .collect(Collectors.toList());
        info.version = campaign.getVersion();
        return info;
    }

    public FirmwareCampaign create(FirmwareCampaignInfo info){
        DeviceType deviceType = null;
        if (info.deviceType != null && info.deviceType.id != null){
            deviceType = deviceConfigurationService.findDeviceType(info.deviceType.id).orElse(null);
        }
        EndDeviceGroup deviceGroup = null;
        if (info.deviceGroup != null && info.deviceGroup.id != null){
            deviceGroup = meteringGroupsService.findEndDeviceGroup(info.deviceGroup.id).orElse(null);
        }
        // Initializing the messageIdentifier property
        if (info.managementOption == null || info.managementOption.id == null) {
            throw exceptionFactory.newException(MessageSeeds.NOT_ABLE_TO_CREATE_CAMPAIGN);
        }
        DeviceMessageId firmwareMessageId = resourceHelper.findFirmwareMessageIdOrThrowException(deviceType, info.managementOption.id);
        if (deviceMessageSpecificationService.needsImageIdentifierAtFirmwareUpload(firmwareMessageId)){
            Long firmwareVersionId = new Long((Integer) info.getPropertyInfo(FirmwareMessageInfoFactory.PROPERTY_KEY_FIRMWARE_VERSION).get().propertyValueInfo.value);
            firmwareService.getFirmwareVersionById(firmwareVersionId).ifPresent(x -> info.getPropertyInfo(FirmwareMessageInfoFactory.PROPERTY_KEY_IMAGE_IDENTIFIER).ifPresent(y -> y.propertyValueInfo = new PropertyValueInfo<>(x.getImageIdentifier(), x.getImageIdentifier(), x.getImageIdentifier() != null)));
        }
        // Initializing the resume property
        info.getPropertyInfo(FirmwareMessageInfoFactory.PROPERTY_KEY_RESUME).ifPresent(x -> x.propertyValueInfo =new PropertyValueInfo<>(false, false, true));

        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, deviceGroup);
        info.writeTo(firmwareCampaign, firmwareMessageInfoFactory);
        return firmwareCampaign;
    }
}
