/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignBuilder;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import javax.inject.Inject;
import java.time.ZoneOffset;

public class TimeOfUseCampaignInfoFactory {

    TimeOfUseCampaignService timeOfUseCampaignService;

    @Inject
    public TimeOfUseCampaignInfoFactory(TimeOfUseCampaignService timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;

    }

    public TimeOfUseCampaign build(TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        TimeOfUseCampaignBuilder timeOfUseCampaignBuilder = timeOfUseCampaignService.newToUbuilder(timeOfUseCampaignInfo.name,
                ((Integer) timeOfUseCampaignInfo.deviceType.id).longValue(), timeOfUseCampaignInfo.deviceGroup, timeOfUseCampaignInfo.activationStart,
                timeOfUseCampaignInfo.activationEnd, ((Integer) timeOfUseCampaignInfo.calendar.id).longValue(), timeOfUseCampaignInfo.activationOption,
                timeOfUseCampaignInfo.activationDate, timeOfUseCampaignInfo.updateType, timeOfUseCampaignInfo.timeValidation);
        return timeOfUseCampaignBuilder.create();
    }

    public TimeOfUseCampaignInfo from(TimeOfUseCampaign campaign) {
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = new TimeOfUseCampaignInfo();
        timeOfUseCampaignInfo.name = campaign.getName();
        timeOfUseCampaignInfo.deviceType = new IdWithNameInfo(campaign.getDeviceType().getId(), campaign.getDeviceType().getName());
        timeOfUseCampaignInfo.deviceGroup = campaign.getDeviceGroup();
        timeOfUseCampaignInfo.activationStart = campaign.getActivationStart();
        timeOfUseCampaignInfo.activationEnd = campaign.getActivationEnd();
        timeOfUseCampaignInfo.timeBoundary = "Between " + campaign.getActivationStart().atZone(ZoneOffset.systemDefault()).toString().substring(11, 16)
                + " and " + campaign.getActivationEnd().atZone(ZoneOffset.systemDefault()).toString().substring(11, 16);
        timeOfUseCampaignInfo.calendar = new IdWithNameInfo(campaign.getCalendar().getId(), campaign.getCalendar().getName());
        timeOfUseCampaignInfo.updateType = campaign.getUpdateType();
//        if (((campaign.getActivationDate().equals("Immediately") || (campaign.getActivationDate().equals("Without Activation"))))) {
//            timeOfUseCampaignInfo.activationDate = campaign.getActivationDate();
//        } else {
//            timeOfUseCampaignInfo.activationDate = ToUUtil.parseStringToInstant(campaign.getActivationDate()).getEpochSecond() + "000";
//        }
        timeOfUseCampaignInfo.activationOption = campaign.getActivationOption();
        timeOfUseCampaignInfo.activationDate = campaign.getActivationDate();
        timeOfUseCampaignInfo.timeValidation = campaign.getTimeValidation();
        timeOfUseCampaignInfo.id = campaign.getId();
        return timeOfUseCampaignInfo;
    }
}
