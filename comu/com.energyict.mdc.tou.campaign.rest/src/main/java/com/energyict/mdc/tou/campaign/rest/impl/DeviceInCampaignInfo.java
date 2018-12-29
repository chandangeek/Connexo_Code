/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import java.time.Instant;

public class DeviceInCampaignInfo {
    String Mrid;
    String status;
    Instant startedOn;
    Instant finishedOn;

    public DeviceInCampaignInfo(String mrid, String status, Instant startedOn, Instant finishedOn) {
        Mrid = mrid;
        this.status = status;
        this.startedOn = startedOn;
        this.finishedOn = finishedOn;
    }
}
