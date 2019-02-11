/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.time.Instant;
import java.util.List;

public class TimeOfUseCampaignInfo {

    public long id;
    public String name;
    public String status;
    public List<DevicesStatusAndQuantity> devices;
    public IdWithNameInfo deviceType;
    public String deviceGroup;
    public Instant activationStart;
    public Instant activationEnd;
    public Instant startedOn;
    public Instant finishedOn;
    public IdWithNameInfo calendar;
    public String activationOption;
    public Instant activationDate;
    public String updateType;
    public long validationTimeout;
    public List<PropertyInfo> properties;
    public long version;
}
