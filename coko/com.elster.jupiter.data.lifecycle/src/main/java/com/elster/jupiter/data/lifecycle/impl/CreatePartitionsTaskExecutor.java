/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.ResultWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class CreatePartitionsTaskExecutor implements TaskExecutor {

    private final LifeCycleServiceImpl lifeCycleService;
    private final EventService eventService;

    CreatePartitionsTaskExecutor(LifeCycleServiceImpl service, EventService eventService) {
        this.lifeCycleService = service;
        this.eventService = eventService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        try {
            Logger logger = Logger.getAnonymousLogger();
            logger.addHandler(occurrence.createTaskLogHandler().asHandler());
            ResultWrapper<String> result = lifeCycleService.createPartitions(logger);
            if (!result.getFailedObjects().isEmpty()) {
                postFailEvent(eventService, occurrence, "Partitions haven't been created for the following tables. Please check logs. [" +
                        String.join(",", result.getFailedObjects()) + "]");
                return;
            }
        } catch (Exception e) {
            postFailEvent(eventService, occurrence, e.getLocalizedMessage());
            return;
        }
    }
}
