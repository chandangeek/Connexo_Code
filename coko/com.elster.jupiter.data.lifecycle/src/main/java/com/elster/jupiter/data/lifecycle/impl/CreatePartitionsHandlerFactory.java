/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.tasks.TaskService;
import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.data.lifecycle.impl.CreatePartitionsHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + Installer.CREATE_PARTITIONS_DESTINATION_NAME,
                "destination=" + Installer.CREATE_PARTITIONS_DESTINATION_NAME},
        immediate = true)
public class CreatePartitionsHandlerFactory implements MessageHandlerFactory {

    private volatile TaskService taskService;
    private volatile LifeCycleServiceImpl lifeCycleService;
    private volatile EventService eventService;

    public CreatePartitionsHandlerFactory() {
        // for OSGI purpose
    }

    @Inject
    public CreatePartitionsHandlerFactory(TaskService taskService, LifeCycleService lifeCycleService, EventService eventService) {
        setTaskService(taskService);
        setLifeCycleService(lifeCycleService);
        setEventService(eventService);
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = (LifeCycleServiceImpl) lifeCycleService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new CreatePartitionsTaskExecutor(lifeCycleService, eventService));
    }
}
