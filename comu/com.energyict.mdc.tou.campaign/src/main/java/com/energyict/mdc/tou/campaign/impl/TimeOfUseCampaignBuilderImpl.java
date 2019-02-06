/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignBuilder;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseCampaignDomainExtension;

import java.time.Instant;
import java.util.Optional;

public class TimeOfUseCampaignBuilderImpl implements TimeOfUseCampaignBuilder {

    public String name;
    public DeviceType deviceType;
    public String deviceGroup;
    public Instant activationStart;
    public Instant activationEnd;
    public Calendar calendar;
    public String activationOption;
    public Instant activationDate;
    public String updateType;
    public long validationTimeout;

    public TimeOfUseCampaignBuilderImpl(String name, DeviceType deviceType, String deviceGroup,
                                        Instant activationStart, Instant activationEnd, Calendar calendar,
                                        String activationOption, Instant activationDate, String updateType, long validationTimeout) {
        this.name = name;
        this.deviceType = deviceType;
        this.deviceGroup = deviceGroup;
        this.activationStart = activationStart;
        this.activationEnd = activationEnd;
        this.calendar = calendar;
        this.activationDate = activationDate;
        this.activationOption = activationOption;
        this.updateType = updateType;
        this.validationTimeout = validationTimeout;
    }

    @Override
    public TimeOfUseCampaign create() {
        TimeOfUseCampaignDomainExtension timeOfUseCampaign = new TimeOfUseCampaignDomainExtension();
        timeOfUseCampaign.setName(name);
        timeOfUseCampaign.setDeviceType(deviceType);
        timeOfUseCampaign.setDeviceGroup(deviceGroup);
        timeOfUseCampaign.setActivationStart(activationStart);
        timeOfUseCampaign.setActivationEnd(activationEnd);
        timeOfUseCampaign.setCalendar(calendar);
        timeOfUseCampaign.setUpdateType(updateType);
        timeOfUseCampaign.setActivationOption(activationOption);
        Optional.ofNullable(activationDate).ifPresent(timeOfUseCampaign::setActivationDate);
        Optional.ofNullable(validationTimeout).ifPresent(timeOfUseCampaign::setValidationTimeout);
        return timeOfUseCampaign;
    }


}
