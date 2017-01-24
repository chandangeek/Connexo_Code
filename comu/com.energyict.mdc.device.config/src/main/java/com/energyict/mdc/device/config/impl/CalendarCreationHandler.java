package com.energyict.mdc.device.config.impl;


import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.energyict.mdc.device.config.calendar.createHandler", service = TopicHandler.class,immediate = true)
public class CalendarCreationHandler implements TopicHandler {

    private volatile DeviceConfigurationService deviceConfigurationService;

    public CalendarCreationHandler() {
    }

    @Inject
    CalendarCreationHandler(DeviceConfigurationService deviceConfigurationService) {
        this();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        Calendar calendar = (Calendar) localEvent.getSource();
        List<AllowedCalendar> allowedCalendars =
                deviceConfigurationService.findAllowedCalendars(calendar.getName());
        for (AllowedCalendar allowedCalendar : allowedCalendars) {
            ((AllowedCalendarImpl) allowedCalendar).replaceGhostBy(calendar);
        }
    }

    @Reference
    public void setSchedulingService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public String getTopicMatcher() {
        return EventType.CALENDAR_CREATE.topic();
    }
}


