/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;

import java.time.Clock;
import java.util.Locale;
import java.util.logging.Logger;

class DataQualityKpiCalculatorHandler implements TaskExecutor {

    private final DataValidationKpiService dataValidationKpiService;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;
    private final ValidationService validationService;
    private final EstimationService estimationService;
    private final Clock clock;
    private final User user;


    DataQualityKpiCalculatorHandler(DataValidationKpiService dataValidationKpiService, TransactionService transactionService, ThreadPrincipalService threadPrincipalService,
                                    ValidationService validationService, EstimationService estimationService, Clock clock, User user) {
        this.dataValidationKpiService = dataValidationKpiService;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.clock = clock;
        this.user = user;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        // do nothing during execute/dequeing transaction - we do our own transaction management - ahum
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
                DataQualityKpiCalculator kpiCalculator = KpiType.calculatorForRecurrentPayload(occurrence, clock, validationService, estimationService, dataValidationKpiService, transactionService, logger);
                kpiCalculator.calculateAndStore();
            } catch (Exception e) {
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
