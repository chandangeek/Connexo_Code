/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.impl.Installer;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.device.data.comschedule.obsolete.backgroundhandler",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + Installer.COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME, "destination=" + Installer.COMSCHEDULE_BACKGROUND_OBSOLETION_MESSAGING_NAME},
        immediate = true)
public class ComScheduleBackgroundObsoleteHandlerFactory implements MessageHandlerFactory {
    private volatile JsonService jsonService;
    private volatile EventService eventService;
    private volatile ServerCommunicationTaskService communicationTaskService;

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setCommunicationTaskService(ServerCommunicationTaskService deviceService) {
        this.communicationTaskService = deviceService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new ComScheduleBackgroundObsoleteHandler(this.jsonService, this.eventService, this.communicationTaskService);
    }

}