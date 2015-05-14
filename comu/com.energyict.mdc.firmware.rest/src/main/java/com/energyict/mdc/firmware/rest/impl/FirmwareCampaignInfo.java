package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    public FirmwareVersionInfo firmwareVersion;
    public Instant plannedDate;
    public Instant startedOn;
    public Instant finishedOn;
    public List<IdWithNameInfo> devices;
    public Map<String, Object> properties;

    public FirmwareCampaignInfo (FirmwareCampaign campaign, Thesaurus thesaurus){
        this.id = campaign.getId();
        this.name = campaign.getName();
        this.status = campaign.getStatus();
        this.deviceType = new IdWithNameInfo(campaign.getDeviceType());
        String upgradeOptionId = campaign.getUpgradeOption().getId();
        this.upgradeOption = thesaurus.getString(upgradeOptionId, upgradeOptionId);
        this.firmwareType = new FirmwareTypeInfo(campaign.getFirmwareType(), thesaurus);
        this.firmwareVersion = FirmwareVersionInfo.from(campaign.getFirmwareVersion(), thesaurus);
        this.plannedDate = campaign.getPlannedDate();
        this.startedOn = campaign.getStartedOn();
        this.finishedOn = campaign.getFinishedOn();
        this.devices = campaign.getDevices().stream().map(DeviceInFirmwareCampaign::getDevice).map(IdWithNameInfo::new).collect(Collectors.toList());
        this.properties = campaign.getProperties(); //TODO use property framework
    }

    public void writeTo(FirmwareCampaign campaign, ResourceHelper resourceHelper){
        campaign.setName(this.name);
        ProtocolSupportedFirmwareOptions.from(this.upgradeOption).ifPresent(upgradeOption -> {
            campaign.setUpgradeOption(upgradeOption);
        });
        if (this.firmwareType != null){
            campaign.setFirmwareType(this.firmwareType.id);
        }
        if (this.firmwareVersion != null){
            campaign.setFirmwareVersion(resourceHelper.findFirmwareVersionByIdOrThrowException(this.firmwareVersion.id));
        }
        campaign.setPlannedDate(this.plannedDate);
        campaign.clearProperties();
        if (this.properties != null){
            for (Map.Entry<String, Object> property : this.properties.entrySet()) {
                campaign.addProperty(property.getKey(), property.getValue());
            }
        }
    }

    public FirmwareCampaign create(FirmwareService firmwareService, ResourceHelper resourceHelper){
        long deviceTypeId = this.deviceType != null ? this.deviceType.id: 0;
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
        long deviceGroupId = this.deviceGroup != null ? this.deviceGroup.id : 0;
        EndDeviceGroup deviceGroup = resourceHelper.findDeviceGroupOrThrowException(deviceGroupId);
        FirmwareCampaign firmwareCampaign = firmwareService.newFirmwareCampaign(deviceType, deviceGroup);
        writeTo(firmwareCampaign, resourceHelper);
        return firmwareCampaign;
    }
}
