package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarResolver;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;



@Component(name = "com.energyict.mdc.device.config.calendarResolver", service = CalendarResolver.class)
public class DeviceConfigCalendarResolver implements CalendarResolver {

    private volatile DeviceConfigurationService deviceConfigurationService;


    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public boolean isCalendarInUse(Calendar calendar) {
        return !deviceConfigurationService.findDeviceTypesForCalendar(calendar.getId()).isEmpty();
    }
}

