/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.List;

public class DeviceTypeAndOptionsInfo {
    public IdWithNameInfo deviceType;
    public List<IdWithNameInfo> calendars;
    public boolean withActivationDate;
    public boolean fullCalendar;
    public boolean specialDays;
}
