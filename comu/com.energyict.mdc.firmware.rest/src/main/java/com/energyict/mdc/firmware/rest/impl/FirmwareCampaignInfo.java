package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareCampaignInfo {
    public long id;
    public String name;
    public IdWithLocalizedValue<String> status;
    public IdWithLocalizedValue<Long> deviceType;
    public IdWithLocalizedValue<Long> deviceGroup;
    public ManagementOptionInfo managementOption;
    public FirmwareTypeInfo firmwareType;
    public FirmwareVersionInfo firmwareVersion;
    public Instant plannedDate;
    public Instant startedOn;
    public Instant finishedOn;
    public List<PropertyInfo> properties;
    public List<DeviceInFirmwareCampaignStatusInfo> devicesStatus;

    public FirmwareCampaignInfo() {}

    public void writeTo(FirmwareCampaign campaign){
        campaign.setName(this.name);
        if (this.managementOption != null) {
            ProtocolSupportedFirmwareOptions.from(this.managementOption.id).ifPresent(managementOption ->
                campaign.setManagementOption(managementOption)
            );
        }
        if (this.firmwareType != null) {
            campaign.setFirmwareType(this.firmwareType.id);
        }
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
}
