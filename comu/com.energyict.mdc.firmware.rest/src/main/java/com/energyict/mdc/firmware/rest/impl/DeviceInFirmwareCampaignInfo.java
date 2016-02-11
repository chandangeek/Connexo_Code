package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;

import java.time.Instant;

public class DeviceInFirmwareCampaignInfo {
    public String mrid;
    public IdWithNameInfo status;
    public Instant startedOn;
    public Instant finishedOn;

    public DeviceInFirmwareCampaignInfo() {
    }

    public DeviceInFirmwareCampaignInfo(DeviceInFirmwareCampaign device, Thesaurus thesaurus) {
        this.mrid = device.getDevice().getmRID();
        if (device.getStatus() != null) {
            this.status = new IdWithNameInfo();
            this.status.id = device.getStatus().key();
            this.status.name = thesaurus.getString(MessageSeeds.Keys.FIRMWARE_MANAGEMENT_DEVICE_STATUS_PREFIX + device.getStatus().key(), device.getStatus().key());
        }
        this.startedOn = device.getStartedOn();
        this.finishedOn = device.getFinishedOn();
    }
}
