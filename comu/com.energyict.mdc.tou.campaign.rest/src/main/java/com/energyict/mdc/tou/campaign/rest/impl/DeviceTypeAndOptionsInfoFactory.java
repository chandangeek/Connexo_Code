/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import javax.inject.Inject;
import java.util.ArrayList;

public class DeviceTypeAndOptionsInfoFactory {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    DeviceTypeAndOptionsInfoFactory(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    DeviceTypeAndOptionsInfo create(long deviceTypeId, TimeOfUseCampaignService timeOfUseCampaignService) {
        DeviceTypeAndOptionsInfo deviceTypeAndOptionsInfo = new DeviceTypeAndOptionsInfo();
        DeviceType deviceType = timeOfUseCampaignService.getDeviceTypesWithCalendars().stream()
                .filter(deviceType1 -> deviceType1.getId() == deviceTypeId).findAny().get();
        deviceTypeAndOptionsInfo.deviceType = new IdWithNameInfo(deviceType.getId(), deviceType.getName());
        deviceTypeAndOptionsInfo.calendars = new ArrayList<>();
        deviceTypeAndOptionsInfo.fullCalendar = false;
        deviceTypeAndOptionsInfo.withActivationDate = false;
        deviceTypeAndOptionsInfo.specialDays = false;
        deviceType.getAllowedCalendars().stream()
                .filter(allowedCalendar -> !allowedCalendar.isGhost())
                .forEach(allowedCalendar -> deviceTypeAndOptionsInfo.calendars.add(new IdWithNameInfo(allowedCalendar.getCalendar().get().getId(), allowedCalendar.getName())));
        deviceConfigurationService.findTimeOfUseOptions(deviceType).get().getOptions()
                .forEach(protocolSupportedCalendarOptions -> {
                    if (protocolSupportedCalendarOptions.getId().equals("send")) {
                        deviceTypeAndOptionsInfo.fullCalendar = true;
                    } else if (protocolSupportedCalendarOptions.getId().equals("sendWithDateTime")) {
                        deviceTypeAndOptionsInfo.fullCalendar = true;
                        deviceTypeAndOptionsInfo.withActivationDate = true;
                    } else if (protocolSupportedCalendarOptions.getId().equals("sendSpecialDays")) {
                        deviceTypeAndOptionsInfo.specialDays = true;
                    }
                });
        return deviceTypeAndOptionsInfo;
    }
}
