/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;

import java.time.Instant;

public class DeviceInFirmwareCampaignInfo {

    public long campaignId;
    public String deviceName;
    public IdWithNameInfo status;
    public Instant startedOn;
    public Instant finishedOn;

    public DeviceInFirmwareCampaignInfo() {
    }

    public DeviceInFirmwareCampaignInfo(DeviceInFirmwareCampaign deviceInFirmwareCampaign, Thesaurus thesaurus) {
        this.campaignId = deviceInFirmwareCampaign.getFirmwareCampaign().getId();
        this.deviceName = deviceInFirmwareCampaign.getDevice().getName();
        if (deviceInFirmwareCampaign.getStatus() != null) {
            this.status = new IdWithNameInfo();
            this.status.id = deviceInFirmwareCampaign.getStatus().key();
            this.status.name = thesaurus.getString(MessageSeeds.Keys.FIRMWARE_MANAGEMENT_DEVICE_STATUS_PREFIX + deviceInFirmwareCampaign.getStatus().key(), deviceInFirmwareCampaign.getStatus().key());
        }
        this.startedOn = deviceInFirmwareCampaign.getStartedOn();
        this.finishedOn = deviceInFirmwareCampaign.getFinishedOn();
    }
}
