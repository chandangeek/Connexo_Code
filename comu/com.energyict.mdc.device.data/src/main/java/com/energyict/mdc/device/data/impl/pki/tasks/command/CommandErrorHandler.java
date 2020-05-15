package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.logging.Logger;

public class CommandErrorHandler {

    private final TaskExecutor executor;
    private final TaskOccurrence taskOccurence;
    private final EventService eventService;
    private final Logger logger;

    public CommandErrorHandler(TaskExecutor taskExecutor, TaskOccurrence taskOccurrence, EventService eventService, Logger logger) {
        this.executor = taskExecutor;
        this.taskOccurence = taskOccurrence;
        this.eventService = eventService;
        this.logger = logger;
    }

    public void handle(CommandAbortException e) {
        this.logger.warning(e.getMessage());
        this.executor.postFailEvent(eventService, taskOccurence, e.getMessage());
    }

    public void handle(CommandErrorException e) {
        this.logger.severe(e.getMessage());
        this.executor.postFailEvent(eventService, taskOccurence, e.getMessage());
    }
}
