package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;
import com.elster.jupiter.validation.kpi.DataValidationReportService;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

class DataManagementKpiCalculatorHandler implements TaskExecutor {

    private final DataValidationKpiService dataValidationKpiService;
    private final DataValidationReportService dataValidationReportService;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;
    private final Clock clock;
    private final User user;



    DataManagementKpiCalculatorHandler(DataValidationKpiService dataValidationKpiService, TransactionService transactionService, ThreadPrincipalService threadPrincipalService, DataValidationReportService dataValidationReportService, Clock clock, User user) {
        this.dataValidationKpiService = dataValidationKpiService;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.dataValidationReportService = dataValidationReportService;
        this.clock = clock;
        this.user = user;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        if (taskOccurrence.getStatus().equals(TaskStatus.BUSY) && taskOccurrence.getRecurrentTask().getNextExecution() == null) {
            return;
        }
    }


    @Override
    public void postExecute(TaskOccurrence taskOccurrence) {
        if (taskOccurrence.getStatus().equals(TaskStatus.BUSY) && taskOccurrence.getRecurrentTask().getNextExecution() == null) {
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
               // transactionService.run(() -> occurrence.getRecurrentTask().updateLastRun(occurrence.getTriggerTime()));
            }
        }
    }

    private void tryExecute(TaskOccurrence taskOccurrence, Logger taskLogger) {
        DataManagementKpiCalculator kpiCalculator = KpiType.calculatorForRecurrentPayload(taskOccurrence, new ServiceProvider());
        kpiCalculator.calculate();
        ((DataValidationKpiCalculator) kpiCalculator).getDataValidationKpi().getDeviceGroup().getMembers(Instant.now(clock)).forEach(endDevice -> doCalculateTransactional(kpiCalculator,endDevice.getId(), taskLogger));

    }

    private void doCalculateTransactional(DataManagementKpiCalculator dataManagementKpiCalculator, long endDeviceId, Logger taskLogger) {
        try {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                dataManagementKpiCalculator.store(endDeviceId);
                transactionContext.commit();
            }
        } catch (Exception ex) {
            transactionService.run(() -> taskLogger.log(Level.WARNING, "Failed to calculate Data Validation KPI for device having ID of" + endDeviceId
                    + " . Error: " + ex.getLocalizedMessage(), ex));
        }
    }

    private Logger createTaskLogger(TaskOccurrence occurrence) {
        Logger taskLogger = Logger.getLogger(DataManagementKpiCalculatorHandler.class.getName() + '.' + occurrence.getId());
        taskLogger.addHandler(occurrence.createTaskLogHandler().asHandler());
        return taskLogger;
    }


    private class ServiceProvider implements KpiType.ServiceProvider {

        @Override
        public DataValidationKpiService dataValidationKpiService() {
            return dataValidationKpiService;
        }

        @Override
        public DataValidationReportService dataValidationReportService() {
            return dataValidationReportService;
        }

        @Override
        public Clock getClock() {
            return clock;
        }
    }

}