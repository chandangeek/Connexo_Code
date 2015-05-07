package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.streams.Functions;

import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 5/05/2015
 * Time: 15:05
 */
class EstimationTaskExecutor implements TaskExecutor {

    private final IEstimationService estimationService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    public EstimationTaskExecutor(IEstimationService estimationService, TransactionService transactionService, Thesaurus thesaurus) {
        this.estimationService = estimationService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        try (LoggingContext loggingContext = LoggingContext.get()) {
            Logger taskLogger = createTaskLogger(occurrence);
            try {
                tryExecute(occurrence, taskLogger);
            } catch (Exception e) {
                loggingContext.severe(taskLogger, e);
            }
        }
    }

    private void tryExecute(TaskOccurrence occurrence, Logger taskLogger) {
        EstimationTask estimationTask = getEstimationTask(occurrence);
        estimationTask.getEndDeviceGroup().getMembers(occurrence.getTriggerTime()).stream()
                .filter(device -> device instanceof Meter)
                .map(device -> ((Meter) device).getMeterActivation(occurrence.getTriggerTime()))
                .flatMap(Functions.asStream())
                .forEach((meterActivation) -> estimationService.estimate(meterActivation, taskLogger));
        estimationTask.updateLastRun(occurrence.getTriggerTime());
    }

    private EstimationTask getEstimationTask(TaskOccurrence occurrence) {
        RecurrentTask recurrentTask = occurrence.getRecurrentTask();
        return estimationService.findEstimationTask(recurrentTask).orElseThrow(IllegalArgumentException::new);
    }

    private Logger createTaskLogger(TaskOccurrence occurrence) {
        Logger taskLogger = Logger.getLogger(EstimationTaskExecutor.class.getName() + '.' + occurrence.getId());
        taskLogger.addHandler(occurrence.createTaskLogHandler().asHandler());
        return taskLogger;
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
    }

}
