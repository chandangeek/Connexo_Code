/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.autoreschedule.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Performs a "bulk run now" for the failed communication task executions configured in {@link AutoRescheduleTaskFactory}
 */
class FailedComTasksTrigger {
    private static final Logger LOGGER = Logger.getLogger(FailedComTasksTrigger.class.getName());
    private final TaskParametersProvider taskParametersProvider;
    private final long taskInterval;

    FailedComTasksTrigger(TaskParametersProvider taskParametersProvider) {
        this.taskParametersProvider = taskParametersProvider;
        taskInterval = taskParametersProvider.getTaskInterval();
    }

    long runNow() {
        AtomicLong nbOfRescheduledFailedComTaskExecs = new AtomicLong(0);
        taskParametersProvider.getComTaskExecutionsForDevicesByComTask().
                forEach(cte -> {
                    if (shouldRunComTask(cte)) {
                        cte.runNow();
                        nbOfRescheduledFailedComTaskExecs.incrementAndGet();
                    }
                });

        return nbOfRescheduledFailedComTaskExecs.get();
    }

    private boolean shouldRunComTask(ComTaskExecution cte) {
        int comTaskInterval = cte.getNextExecutionSpecs()
                .flatMap(nextExecutionSpecs -> Optional.of(nextExecutionSpecs.getTemporalExpression()))
                .map(temporalExpression -> temporalExpression.getEvery().getSeconds())
                .orElse(0);

        if (comTaskInterval <= taskInterval) {
            LOGGER.fine("FAILED comtask " + cte.getComTask().getName() + " filtered out because it's more frequent than autoretry task (" +
                    comTaskInterval + " <= " + taskInterval + ")");
        }
        return comTaskInterval > taskInterval;
    }
}
