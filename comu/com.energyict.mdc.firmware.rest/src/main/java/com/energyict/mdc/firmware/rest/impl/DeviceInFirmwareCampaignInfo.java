package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;

import java.time.Instant;

public class DeviceInFirmwareCampaignInfo {
    public long id;
    public String name;
    public com.energyict.mdc.common.rest.IdWithNameInfo status;
    public Instant startedOn;
    public Instant finishedOn;

    public DeviceInFirmwareCampaignInfo() {
    }

    public DeviceInFirmwareCampaignInfo(DeviceInFirmwareCampaign device, Thesaurus thesaurus) {
        this.id = device.getDevice().getId();
        this.name = device.getDevice().getmRID();
        if (device.getStatus() != null) {
            this.status = new com.energyict.mdc.common.rest.IdWithNameInfo();
            this.status.id = device.getStatus().key();
            this.status.name = device.getStatus().translate(thesaurus);
        }
    }
}
