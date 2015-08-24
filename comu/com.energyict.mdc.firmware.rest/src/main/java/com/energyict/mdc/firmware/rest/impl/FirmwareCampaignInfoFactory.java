package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FirmwareCampaignInfoFactory {
    private final Thesaurus thesaurus;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final FirmwareService firmwareService;
    private final MeteringGroupsService meteringGroupsService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final FirmwareVersionInfoFactory firmwareVersionFactory;

    @Inject
    public FirmwareCampaignInfoFactory(
            Thesaurus thesaurus,
            MdcPropertyUtils mdcPropertyUtils,
            FirmwareService firmwareService,
            MeteringGroupsService meteringGroupsService,
            DeviceConfigurationService deviceConfigurationService,
            FirmwareVersionInfoFactory firmwareVersionFactory) {
        this.thesaurus = thesaurus;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.firmwareService = firmwareService;
        this.meteringGroupsService = meteringGroupsService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.firmwareVersionFactory = firmwareVersionFactory;
    }

    public FirmwareCampaignInfo from(FirmwareCampaign campaign){
        FirmwareCampaignInfo info = new FirmwareCampaignInfo();
        info.id = campaign.getId();
        info.name = campaign.getName();
        FirmwareCampaignStatus campaignStatus = campaign.getStatus();
        info.status = new IdWithLocalizedValue();
        info.status.id = campaignStatus.name();
        String statusTranslationKey = new FirmwareCampaignStatusAdapter().marshal(campaignStatus);
        info.status.localizedValue = thesaurus.getString(statusTranslationKey, statusTranslationKey);
        info.deviceType = IdWithLocalizedValue.from(campaign.getDeviceType());
        String managementOptionId = campaign.getFirmwareManagementOption().getId();
        info.managementOption = new ManagementOptionInfo(managementOptionId, thesaurus.getString(managementOptionId, managementOptionId));
        info.firmwareType = new FirmwareTypeInfo(campaign.getFirmwareType(), thesaurus);
        info.startedOn = campaign.getStartedOn();
        info.finishedOn = campaign.getFinishedOn();
        Optional<DeviceMessageSpec> firmwareMessageSpec = campaign.getFirmwareMessageSpec();
        if (firmwareMessageSpec.isPresent()) {
            TypedProperties typedProperties = TypedProperties.empty();
            Map<String, Object> properties = campaign.getProperties();
            for (Map.Entry<String, Object> property : properties.entrySet()) {
                typedProperties.setProperty(property.getKey(), property.getValue());
            }
            PropertyDefaultValuesProvider provider = (propertySpec, propertyType) -> {
                if (FirmwareVersion.class.equals(propertySpec.getValueFactory().getValueType())){
                    FirmwareVersionFilter filter = new FirmwareVersionFilter(campaign.getDeviceType());
                    filter.addFirmwareTypes(Arrays.asList(campaign.getFirmwareType()));
                    filter.addFirmwareStatuses(Arrays.asList(FirmwareStatus.FINAL, FirmwareStatus.TEST));
                    return firmwareService.findAllFirmwareVersions(filter).find();
                }
                return null;
            };
            info.firmwareVersion = firmwareVersionFactory.from(campaign.getFirmwareVersion());
            info.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(firmwareMessageSpec.get().getPropertySpecs(), typedProperties, provider);
        }
        info.devicesStatus = campaign.getDevicesStatusMap().entrySet()
                .stream()
                .map(status -> new DeviceInFirmwareCampaignStatusInfo(status.getKey(), status.getValue(), thesaurus))
                .collect(Collectors.toList());
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
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, deviceGroup);
        info.writeTo(firmwareCampaign, mdcPropertyUtils);
        return firmwareCampaign;
    }
}
