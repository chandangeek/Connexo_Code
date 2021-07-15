/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.logging.Logger;


public class LifeCycleTaskExecutor implements TaskExecutor {

    private final LifeCycleServiceImpl lifeCycleService;
    private final EventService eventService;

    LifeCycleTaskExecutor(LifeCycleServiceImpl service, EventService eventService) {
        this.lifeCycleService = service;
        this.eventService = eventService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        try {
            Logger logger = Logger.getAnonymousLogger();
            logger.addHandler(occurrence.createTaskLogHandler().asHandler());
            lifeCycleService.purgeData(logger);
        } catch (Exception e){
            postFailEvent(eventService, occurrence, e.getLocalizedMessage());
            throw e;
        }

    }
}
