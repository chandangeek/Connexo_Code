/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.firmware.FirmwareCampaignVersionState;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareCampaignVersionStateInfo {
    public String firmwareVersion;
    public String firmwareType;
    public String firmwareStatus;
    public String imageIdentifier;
    public String rank;
    public IdWithNameInfo meterFirmwareDependency;
    public IdWithNameInfo communicationFirmwareDependency;
}
