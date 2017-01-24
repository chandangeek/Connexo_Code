package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.exceptions.VetoCalendarDeleteException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


@Component(name = "com.energyict.mdc.device.config.calendar.deletionHandler", service = TopicHandler.class,immediate = true)
public class CalendarDeletionHandler implements TopicHandler {

    private volatile ServerDeviceConfigurationService deviceConfigurationService;

    public CalendarDeletionHandler() {
    }

    @Override
    public void handle(LocalEvent localEvent) {
        Calendar calendar = (Calendar) localEvent.getSource();
        if (isInUse(calendar)) {
            throw new VetoCalendarDeleteException(deviceConfigurationService.getThesaurus(), calendar);
        }
    }

    boolean isInUse(Calendar calendar) {
        return deviceConfigurationService.getAllowedCalendarsQuery()
                    .filter(Operator.EQUAL.compare(AllowedCalendarImpl.Fields.CALENDAR.fieldName(), calendar))
                    .limit(1)
                    .findAny()
                    .isPresent();
    }

    @Override
    public String getTopicMatcher() {
        return EventType.CALENDAR_DELETE.topic();
    }

    @Reference
    public void setSchedulingService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = (ServerDeviceConfigurationService) deviceConfigurationService;
    }
}
