/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.demo.customtask.impl;

import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.customtask.CustomTaskStatus;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class DemoCustomTaskExecutor implements TaskExecutor {

    private final CustomTaskService customTaskService;
    private final TransactionService transactionService;
    private final MeteringGroupsService meteringGroupsService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private static final Logger LOGGER = Logger.getLogger(DemoCustomTaskExecutor.class.getName());

    DemoCustomTaskExecutor(CustomTaskService customTaskService,
                           TransactionService transactionService,
                           Thesaurus thesaurus,
                           MeteringGroupsService meteringGroupsService,
                           Clock clock) {
        this.customTaskService = customTaskService;
        this.transactionService = transactionService;
        this.meteringGroupsService = meteringGroupsService;
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        customTaskService.createCustomTaskOccurrence(occurrence);
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        CustomTaskOccurrence customTaskOccurrence = findOccurrence(occurrence);

        Logger occurrenceLogger = getLogger(occurrence, occurrence.getRecurrentTask());
        try {
            doExecute(customTaskOccurrence, occurrenceLogger);
        } catch (Exception ex) {
        }
    }

    private Logger getLogger(TaskOccurrence occurrence, RecurrentTask recurrentTask) {
        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(occurrence.createTaskLogHandler(recurrentTask).asHandler());
        return logger;
    }

    private CustomTaskOccurrence findOccurrence(TaskOccurrence occurrence) {
        return customTaskService.findCustomTaskOccurrence(occurrence).orElseThrow(IllegalArgumentException::new);
    }

    private void doExecute(CustomTaskOccurrence occurrence, Logger logger) {
        CustomTask task = occurrence.getTask();
        String groupName = task.getValues().get(TranslationKeys.SELECTOR.getKey()).toString();
        String separator = task.getValues().get(TranslationKeys.SEPARATOR.getKey()).toString();
        long count = Long.parseLong(task.getValues().get(TranslationKeys.COUNT.getKey()).toString());
        List<String> outputLog = new ArrayList<>();

        UsagePointGroup psagePointGroup = meteringGroupsService.findUsagePointGroups()
                .stream()
                .filter(group -> group.getName().compareToIgnoreCase(groupName) == 0)
                .findFirst().get();

        occurrence.summarize(getThesaurus().getFormat(TranslationKeys.CUSTOM_TASK_COMPLETED).format(groupName));

        try (TransactionContext context = transactionService.getContext()) {
            psagePointGroup.getMembers(clock.instant()).stream()
                    .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
                    .limit(count)
                .forEach(endDevice -> {
                    MessageSeeds.DEVICE_PROCESSED.log(logger, thesaurus, endDevice.getName());
                    outputLog.add(endDevice.getName());
                }
            );
            occurrence.end(CustomTaskStatus.SUCCESS);
            occurrence.update();
            context.commit();
        }
        LOGGER.log(Level.INFO, "Devices processed: " + String.join(separator, outputLog));
    }
    private Thesaurus getThesaurus(){
        return thesaurus;
    }
}