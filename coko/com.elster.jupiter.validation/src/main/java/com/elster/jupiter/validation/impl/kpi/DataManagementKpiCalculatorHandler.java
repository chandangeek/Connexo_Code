/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.metering.EndDevice;
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
import java.time.Instant;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

class DataManagementKpiCalculatorHandler implements TaskExecutor {

    private final DataValidationKpiService dataValidationKpiService;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;
    private final ValidationService validationService;
    private final Clock clock;
    private final User user;


    DataManagementKpiCalculatorHandler(DataValidationKpiService dataValidationKpiService, TransactionService transactionService, ThreadPrincipalService threadPrincipalService,
                                       ValidationService validationService, Clock clock, User user) {
        this.dataValidationKpiService = dataValidationKpiService;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.validationService = validationService;
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
            Logger taskLogger = createTaskLogger(occurrence);
            try {
                tryExecute(occurrence, taskLogger);
            } catch (Exception e) {
                transactionService.run(() -> loggingContext.severe(taskLogger, e));
            } finally {
                // should we do the thing below ?? Tom ?
                // transactionService.run(() -> occurrence.getRecurrentTask().updateLastRun(occurrence.getTriggerTime()));
            }
        }
    }

    private void tryExecute(TaskOccurrence taskOccurrence, Logger taskLogger) {
        DataManagementKpiCalculator kpiCalculator = KpiType.calculatorForRecurrentPayload(taskOccurrence, clock, validationService, dataValidationKpiService);
        try {
            transactionService.run(kpiCalculator::calculate);
        } catch (Exception ex) {
            transactionService.run(() -> taskLogger.log(Level.WARNING, "Failed to calculate Data Validation KPI"
                    + " . Error: " + ex.getLocalizedMessage(), ex));
        }
        ((DataValidationKpiCalculator) kpiCalculator).getDataValidationKpi()
                .getDeviceGroup()
                .getMembers(Instant.now(clock))
                .forEach(endDevice -> doCalculateTransactional(kpiCalculator, endDevice, taskLogger));

    }

    private void doCalculateTransactional(DataManagementKpiCalculator dataManagementKpiCalculator, EndDevice endDevice, Logger taskLogger) {
        try {
            transactionService.run(() -> dataManagementKpiCalculator.store(endDevice));
        } catch (Exception ex) {
            transactionService.run(() -> taskLogger.log(Level.WARNING, "Failed to store Validation KPI data for device " + endDevice.getName()
                    + ". Error: " + ex.getLocalizedMessage(), ex));
        }
    }

    private Logger createTaskLogger(TaskOccurrence occurrence) {
        Logger taskLogger = Logger.getLogger(DataManagementKpiCalculatorHandler.class.getName() + '.' + occurrence.getId());
        taskLogger.addHandler(occurrence.createTaskLogHandler().asHandler());
        return taskLogger;
    }


}
