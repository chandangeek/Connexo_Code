/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignBuilder;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.tou.campaign.ToUUtil;

import javax.inject.Inject;

public class TimeOfUseCampaignInfoFactory {

    TimeOfUseCampaignService timeOfUseCampaignService;

    @Inject
    public TimeOfUseCampaignInfoFactory(TimeOfUseCampaignService timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;

    }

    public TimeOfUseCampaign build(TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        TimeOfUseCampaignBuilder timeOfUseCampaignBuilder = timeOfUseCampaignService.newToUbuilder(timeOfUseCampaignInfo.name,
                timeOfUseCampaignInfo.deviceType, timeOfUseCampaignInfo.deviceGroup, timeOfUseCampaignInfo.activationStart,
                timeOfUseCampaignInfo.activationEnd, timeOfUseCampaignInfo.calendar, timeOfUseCampaignInfo.activationDate,
                timeOfUseCampaignInfo.updateType, timeOfUseCampaignInfo.timeValidation);
        return timeOfUseCampaignBuilder.create();
    }

    public TimeOfUseCampaignInfo from(TimeOfUseCampaign campaign) {
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = new TimeOfUseCampaignInfo();
        timeOfUseCampaignInfo.name = campaign.getName();
        timeOfUseCampaignInfo.deviceType = campaign.getDeviceType().getId();
        timeOfUseCampaignInfo.deviceGroup = campaign.getDeviceGroup();
        timeOfUseCampaignInfo.activationStart = campaign.getActivationStart();
        timeOfUseCampaignInfo.activationEnd = campaign.getActivationEnd();
        timeOfUseCampaignInfo.calendar = campaign.getCalendar().getId();
        timeOfUseCampaignInfo.updateType = campaign.getUpdateType();
        if (((campaign.getActivationDate().equals("Immediately") || (campaign.getActivationDate().equals("Without Activation"))))) {
            timeOfUseCampaignInfo.activationDate = campaign.getActivationDate();
        } else {
            timeOfUseCampaignInfo.activationDate = ToUUtil.parseStringToInstant(campaign.getActivationDate()).getEpochSecond() + "000";
        }
        timeOfUseCampaignInfo.timeValidation = campaign.getTimeValidation();
        timeOfUseCampaignInfo.id = campaign.getId();
        return timeOfUseCampaignInfo;
    }
}
