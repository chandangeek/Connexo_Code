/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
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
    public Instant startedOn;
    public Instant finishedOn;
    public Integer timeBoundaryStart;
    public Integer timeBoundaryEnd;
    public List<PropertyInfo> properties;
    public List<DeviceInFirmwareCampaignStatusInfo> devicesStatus;
    public long version;

    public FirmwareCampaignInfo() {}

    public void writeTo(FirmwareCampaign campaign, MdcPropertyUtils mdcPropertyUtils){
        campaign.setName(this.name);
        if (this.managementOption != null) {
            ProtocolSupportedFirmwareOptions.from(this.managementOption.id).ifPresent(managementOption ->
                campaign.setManagementOption(managementOption)
            );
        }
        if (this.firmwareType != null) {
            campaign.setFirmwareType(this.firmwareType.id);
        }

        if (this.timeBoundaryStart != null && this.timeBoundaryEnd != null) {
            campaign.setComWindow(new ComWindow(this.timeBoundaryStart, this.timeBoundaryEnd));
        }

        if (campaign.getFirmwareMessageSpec().isPresent()) {
            campaign.clearProperties();
            for (PropertySpec propertySpec : campaign.getFirmwareMessageSpec().get().getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
                if(propertyValue != null){
                    campaign.addProperty(propertySpec.getName(), propertySpec.getValueFactory().toStringValue(propertyValue));
                }
            }
        }
    }
}
