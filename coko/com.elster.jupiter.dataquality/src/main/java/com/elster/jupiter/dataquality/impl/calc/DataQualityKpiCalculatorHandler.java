/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.estimation.EstimatorNotFoundException;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.validation.ValidatorNotFoundException;

import java.util.Locale;
import java.util.logging.Logger;

class DataQualityKpiCalculatorHandler implements TaskExecutor {

    private final DataQualityServiceProvider serviceProvider;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;
    private final EventService eventService;
    private final User user;

    DataQualityKpiCalculatorHandler(DataQualityServiceProvider serviceProvider, EventService eventService, User user) {
        this.serviceProvider = serviceProvider;
        this.transactionService = serviceProvider.transactionService();
        this.threadPrincipalService = serviceProvider.threadPrincipalService();
        this.eventService = eventService;
        this.user = user;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        // do nothing during execute/dequeing transaction - we do our own transaction management
    }

    @Override
    public void postExecute(TaskOccurrence taskOccurrence) {
        if (TaskStatus.BUSY.equals(taskOccurrence.getStatus()) && taskOccurrence.getRecurrentTask().getNextExecution() == null) {
            // task has been cancelled
            return;
        }
        threadPrincipalService.runAs(user, () -> runCalculator(taskOccurrence), Locale.getDefault());

    }

    private void runCalculator(TaskOccurrence occurrence) {
        try (LoggingContext loggingContext = LoggingContext.getCloseableContext()) {
            Logger logger = createTaskLogger(occurrence);
            try {
                DataQualityKpiCalculator kpiCalculator = KpiType.calculatorForRecurrentPayload(serviceProvider, occurrence, logger);
                kpiCalculator.calculateAndStore();
            } catch (ValidatorNotFoundException | EstimatorNotFoundException e) {
                transactionService.run(() -> loggingContext.warning(logger, "Failed to calculate data quality KPI. Error: " + e.getLocalizedMessage()));
            } catch (Exception e) {
                postFailEvent(eventService, occurrence, e.getLocalizedMessage());
                transactionService.run(() -> loggingContext.severe(logger, e));
            }
        }
    }

    private Logger createTaskLogger(TaskOccurrence occurrence) {
        Logger taskLogger = Logger.getLogger(DataQualityKpiCalculatorHandler.class.getName() + '.' + occurrence.getId());
        taskLogger.addHandler(occurrence.createTaskLogHandler().asHandler());
        return taskLogger;
    }
}
