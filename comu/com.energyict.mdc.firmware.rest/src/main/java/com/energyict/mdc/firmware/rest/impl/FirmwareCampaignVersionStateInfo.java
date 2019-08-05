/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.firmware.FirmwareCampaignVersionState;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareCampaignVersionStateInfo {
    public String firmwareVersion;
    public String firmwareType;
    public String firmwareStatus;
    public String imageIdentifier;
    public String rank;
    public String meterFirmwareDependency;
    public String communicationFirmwareDependency;

    public FirmwareCampaignVersionStateInfo(FirmwareCampaignVersionState firmwareCampaignVersionState){
        this.firmwareVersion = firmwareCampaignVersionState.getFirmwareVersion();
        this.firmwareType = firmwareCampaignVersionState.getFirmwareType();
        this.firmwareStatus = firmwareCampaignVersionState.getFirmwareStatus();
        this.imageIdentifier = firmwareCampaignVersionState.getImageIdentifier();
        this.rank = firmwareCampaignVersionState.getRank();
        this.meterFirmwareDependency = firmwareCampaignVersionState.getMeterFirmwareDependency();
        this.communicationFirmwareDependency = firmwareCampaignVersionState.getCommunicationFirmwareDependency();
    }

}
