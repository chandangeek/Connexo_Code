/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;

public class DeviceInCampaignInfo {
    public IdWithNameInfo device;
    public String status;
    public Instant startedOn;
    public Instant finishedOn;

    public DeviceInCampaignInfo(IdWithNameInfo device, String status, Instant startedOn, Instant finishedOn) {
        this.device = device;
        this.status = status;
        this.startedOn = startedOn;
        this.finishedOn = finishedOn;
    }
}
