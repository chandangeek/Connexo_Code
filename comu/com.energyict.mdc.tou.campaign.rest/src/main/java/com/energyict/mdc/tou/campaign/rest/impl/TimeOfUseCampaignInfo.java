/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.servicecall.DefaultState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TimeOfUseCampaignInfo {

    public long id;
    public String name;
    public String status;
    public List<DevicesStatusAndQuantity> devices;
    public long deviceType;
    public String deviceGroup;
    public Instant activationStart;
    public Instant activationEnd;
    public long calendar;
    public String activationDate;
    public String updateType;
    public long timeValidation;
    public List<PropertyInfo> properties;
    public long version;
}
