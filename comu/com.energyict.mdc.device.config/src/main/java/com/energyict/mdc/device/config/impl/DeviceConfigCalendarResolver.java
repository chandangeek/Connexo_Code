/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarResolver;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;



@Component(name = "com.energyict.mdc.device.config.calendarResolver", service = CalendarResolver.class)
public class DeviceConfigCalendarResolver implements CalendarResolver {

    private volatile ServerDeviceConfigurationService deviceConfigurationService;


    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = (ServerDeviceConfigurationService) deviceConfigurationService;
    }

    @Override
    public boolean isCalendarInUse(Calendar calendar) {
        return deviceConfigurationService.getAllowedCalendarsQuery()
                .filter(Operator.EQUAL.compare(AllowedCalendarImpl.Fields.CALENDAR.fieldName(), calendar))
                .limit(1)
                .findAny()
                .isPresent();
    }
}

