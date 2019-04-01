/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
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

/**
 * Created by bbl on 13/05/2015.
 */
@Component(name = "com.elster.jupiter.data.lifecycle.DataLifecycleHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + Installer.DATA_LIFE_CYCLE_DESTINATION_NAME,
                "destination=" + Installer.DATA_LIFE_CYCLE_DESTINATION_NAME},
        immediate = true)
public class DataLifeCycleHandlerFactory implements MessageHandlerFactory {

    private volatile TaskService taskService;
    private volatile LifeCycleServiceImpl lifeCycleService;
    private volatile EventService eventService;

    public DataLifeCycleHandlerFactory() {
    }

    @Inject
    public DataLifeCycleHandlerFactory(TaskService taskService, LifeCycleService lifeCycleService, EventService eventService) {
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

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference


    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new LifeCycleTaskExecutor(lifeCycleService, eventService));
    }
}