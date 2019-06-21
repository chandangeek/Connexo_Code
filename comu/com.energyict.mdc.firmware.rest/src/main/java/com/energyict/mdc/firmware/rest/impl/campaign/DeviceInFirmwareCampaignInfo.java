/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;

public class DeviceInFirmwareCampaignInfo {

    public long id;
    public IdWithNameInfo device;
    public String status;
    public Instant startedOn;
    public Instant finishedOn;

    public DeviceInFirmwareCampaignInfo(long id, IdWithNameInfo device, String status, Instant startedOn, Instant finishedOn) {
        this.id = id;
        this.device = device;
        this.status = status;
        this.startedOn = startedOn;
        this.finishedOn = finishedOn;
    }
}
