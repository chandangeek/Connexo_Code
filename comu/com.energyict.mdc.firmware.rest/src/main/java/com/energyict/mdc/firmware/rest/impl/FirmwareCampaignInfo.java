package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FirmwareCampaignInfo {
    public long id;
    public String name;
    @XmlJavaTypeAdapter(FirmwareCampaignStatusAdapter.class)
    public FirmwareCampaignStatus status;
    public IdWithNameInfo deviceType;
    public IdWithNameInfo deviceGroup;
    public String upgradeOption;
    public FirmwareTypeInfo firmwareType;
    public Instant plannedDate;
    public Instant startedOn;
    public Instant finishedOn;
    public List<IdWithNameInfo> devices;
    public List<PropertyInfo> properties;

    public FirmwareCampaignInfo() {}

    public FirmwareCampaignInfo (FirmwareCampaign campaign, Thesaurus thesaurus, MdcPropertyUtils mdcPropertyUtils, FirmwareService firmwareService){
        this.id = campaign.getId();
        this.name = campaign.getName();
        this.status = campaign.getStatus();
        this.deviceType = new IdWithNameInfo(campaign.getDeviceType());
        String upgradeOptionId = campaign.getUpgradeOption().getId();
        this.upgradeOption = thesaurus.getString(upgradeOptionId, upgradeOptionId);
        this.firmwareType = new FirmwareTypeInfo(campaign.getFirmwareType(), thesaurus);
        this.plannedDate = campaign.getPlannedDate();
        this.startedOn = campaign.getStartedOn();
        this.finishedOn = campaign.getFinishedOn();
        this.devices = campaign.getDevices().stream().map(DeviceInFirmwareCampaign::getDevice).map(IdWithNameInfo::new).collect(Collectors.toList());
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
                    filter.setFirmwareTypes(Arrays.asList(campaign.getFirmwareType()));
                    filter.setFirmwareStatuses(Arrays.asList(FirmwareStatus.FINAL, FirmwareStatus.TEST));
                    return firmwareService.findAllFirmwareVersions(filter).find();
                }
                return null;
            };
            this.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(firmwareMessageSpec.get().getPropertySpecs(), typedProperties, provider);
        }
    }

    public void writeTo(FirmwareCampaign campaign){
        campaign.setName(this.name);
        ProtocolSupportedFirmwareOptions.from(this.upgradeOption).ifPresent(upgradeOption -> {
            campaign.setUpgradeOption(upgradeOption);
        });
        campaign.setPlannedDate(this.plannedDate);
        campaign.clearProperties();
        if (this.properties != null){
            for (PropertyInfo property : this.properties) {
                if (property.getPropertyValueInfo() != null && property.getPropertyValueInfo().getValue() != null){
                    campaign.addProperty(property.key, property.getPropertyValueInfo().getValue().toString());
                }
            }
        }
    }

    public FirmwareCampaign create(FirmwareService firmwareService, ResourceHelper resourceHelper){
        long deviceTypeId = this.deviceType != null ? this.deviceType.id: 0;
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
        long deviceGroupId = this.deviceGroup != null ? this.deviceGroup.id : 0;
        EndDeviceGroup deviceGroup = resourceHelper.findDeviceGroupOrThrowException(deviceGroupId);
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, deviceGroup);
        writeTo(firmwareCampaign);
        return firmwareCampaign;
    }
}
