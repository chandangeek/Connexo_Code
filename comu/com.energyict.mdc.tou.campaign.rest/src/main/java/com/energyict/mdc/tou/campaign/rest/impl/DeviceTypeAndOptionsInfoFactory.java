/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import javax.inject.Inject;
import java.util.ArrayList;

public class DeviceTypeAndOptionsInfoFactory {

    private final DeviceConfigurationService deviceConfigurationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceTypeAndOptionsInfoFactory(DeviceConfigurationService deviceConfigurationService, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.exceptionFactory = exceptionFactory;
    }

    public DeviceTypeAndOptionsInfo create(DeviceType deviceType) {
        DeviceTypeAndOptionsInfo deviceTypeAndOptionsInfo = new DeviceTypeAndOptionsInfo();
        deviceTypeAndOptionsInfo.deviceType = new IdWithNameInfo(deviceType.getId(), deviceType.getName());
        deviceTypeAndOptionsInfo.calendars = new ArrayList<>();
        deviceTypeAndOptionsInfo.fullCalendar = false;
        deviceTypeAndOptionsInfo.withActivationDate = false;
        deviceTypeAndOptionsInfo.specialDays = false;
        deviceType.getAllowedCalendars().stream()
                .filter(allowedCalendar -> !allowedCalendar.isGhost())
                .forEach(allowedCalendar -> deviceTypeAndOptionsInfo.calendars.add(new IdWithNameInfo(allowedCalendar.getCalendar().get().getId(), allowedCalendar.getName())));
        deviceConfigurationService.findTimeOfUseOptions(deviceType)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_TOU_OPTIONS_ON_DEVICE_TYPE, deviceType.getName()))
                .getOptions()
                .forEach(protocolSupportedCalendarOptions -> {
                    switch (protocolSupportedCalendarOptions.getId()) {
                        case "send":
                            deviceTypeAndOptionsInfo.fullCalendar = true;
                            break;
                        case "sendWithDateTime":
                            deviceTypeAndOptionsInfo.fullCalendar = true;
                            deviceTypeAndOptionsInfo.withActivationDate = true;
                            break;
                        case "sendSpecialDays":
                            deviceTypeAndOptionsInfo.specialDays = true;
                            break;
                    }
                });
        return deviceTypeAndOptionsInfo;
    }
}
