/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.device.data.remove.allowedcalendar.eventhandler", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class AllowedCalendarRemovalEventHandler implements TopicHandler {

    private static final String TOPIC = EventType.ALLOWED_CALENDAR_VALIDATE_DELETE.topic();
    private volatile DeviceDataModelService deviceDataModelService;
    private Thesaurus thesaurus;

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
        this.thesaurus = deviceDataModelService.thesaurus();
    }

    /**
     * Vetos the deletion of the {@link AllowedCalendar}
     * by throwing an exception when there is at least
     * one Device that uses that AllowedCalendar.
     *
     * @param allowedCalendar The AllowedCalendar that is about to be deleted
     */
    private void validateNotUsedByDevice(AllowedCalendar allowedCalendar) {
        if (this.deviceDataModelService.deviceService().hasDevices(allowedCalendar)) {
            throw new VetoObsoleteAllowedCalendarException(this.thesaurus, allowedCalendar);
        }
    }

    @Override
    public void handle(LocalEvent localEvent) {
        AllowedCalendar allowedCalendar = (AllowedCalendar) localEvent.getSource();
        this.validateNotUsedByDevice(allowedCalendar);
    }

    @Override
    public String getTopicMatcher() {
        return TOPIC;
    }
}
