/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.autoreschedule.impl;

import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.customtask.CustomTaskStatus;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;

import java.time.Clock;
import java.util.logging.Logger;

public class AutoRescheduleTaskExecutor implements TaskExecutor {
    private final CustomTaskService customTaskService;
    private final EventService eventService;
    private final TransactionService transactionService;
    private final MeteringGroupsService meteringGroupsService;
    private final CommunicationTaskService communicationTaskService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private static final Logger LOGGER = Logger.getLogger(AutoRescheduleTaskExecutor.class.getName());

    AutoRescheduleTaskExecutor(CustomTaskService customTaskService,
                               EventService eventService, TransactionService transactionService,
                               Thesaurus thesaurus,
                               MeteringGroupsService meteringGroupsService,
                               CommunicationTaskService communicationTaskService, Clock clock) {
        this.customTaskService = customTaskService;
        this.eventService = eventService;
        this.transactionService = transactionService;
        this.meteringGroupsService = meteringGroupsService;
        this.communicationTaskService = communicationTaskService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        LOGGER.info(this.getClass().getName() + " triggered at " + occurrence.getTriggerTime());
        customTaskService.createCustomTaskOccurrence(occurrence);
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        CustomTaskOccurrence customTaskOccurrence = findOccurrence(occurrence);
        Logger occurrenceLogger = getLogger(occurrence, occurrence.getRecurrentTask());

        try {
            doExecute(customTaskOccurrence, occurrenceLogger);
        } catch (Throwable t) {
            postFailEvent(eventService, occurrence, t.getLocalizedMessage());
            try (TransactionContext context = transactionService.getContext()) {
                endTask(customTaskOccurrence, CustomTaskStatus.FAILED);
                context.commit();
            } finally {
                String errorMsg = "Error while executing task \"" + AutoRescheduleTaskFactory.DISPLAY_NAME + "\": " + t;
                postFailEvent(eventService, occurrence, errorMsg);
                LOGGER.severe(errorMsg);
                t.printStackTrace();
            }
        }
    }

    private CustomTaskOccurrence findOccurrence(TaskOccurrence occurrence) {
        return customTaskService.findCustomTaskOccurrence(occurrence).orElseThrow(IllegalArgumentException::new);
    }

    private Logger getLogger(TaskOccurrence occurrence, RecurrentTask recurrentTask) {
        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(occurrence.createTaskLogHandler(recurrentTask).asHandler());

        return logger;
    }

    private void doExecute(CustomTaskOccurrence occurrence, Logger logger) {
        TaskParametersProvider taskParametersContainer = new TaskParametersProvider(meteringGroupsService,
                communicationTaskService, transactionService, clock, occurrence.getTask());
        long nbOfRescheduledComTasks = 0;

        try (TransactionContext context = transactionService.getContext()) {
            nbOfRescheduledComTasks = new FailedComTasksTrigger(taskParametersContainer).runNow();
            MessageSeeds.COMTASKS_PROCESSED.log(logger, thesaurus, nbOfRescheduledComTasks);
            endTask(occurrence, CustomTaskStatus.SUCCESS);
            context.commit();
        }

        occurrence.summarize(getThesaurus()
                .getFormat(TranslationKeys.RETRY_COMTASKS_COMPLETED)
                .format(taskParametersContainer.getEndDeviceGroupName(), nbOfRescheduledComTasks));
    }

    private void endTask(CustomTaskOccurrence customTaskOccurrence, CustomTaskStatus status) {
        customTaskOccurrence.end(status);
    }

    private Thesaurus getThesaurus() {
        return thesaurus;
    }
}
