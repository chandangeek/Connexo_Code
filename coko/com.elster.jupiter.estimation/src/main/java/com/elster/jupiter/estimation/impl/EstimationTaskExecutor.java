package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.logging.Logger;

class EstimationTaskExecutor implements TaskExecutor {

    private final IEstimationService estimationService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final ThreadPrincipalService threadPrincipalService;
    private final User user;

    public EstimationTaskExecutor(IEstimationService estimationService, TransactionService transactionService, Thesaurus thesaurus, TimeService timeService, ThreadPrincipalService threadPrincipalService, UserService userService, User user) {
        this.estimationService = estimationService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.threadPrincipalService = threadPrincipalService;
        this.user = user;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        threadPrincipalService.runAs(user, () -> {
            try (LoggingContext loggingContext = LoggingContext.getCloseableContext()) {
                Logger taskLogger = createTaskLogger(occurrence);
                try {
                    tryExecute(occurrence, taskLogger);
                } catch (Exception e) {
                    loggingContext.severe(taskLogger, e);
                }
            }
        }, Locale.getDefault());
    }

    private void tryExecute(TaskOccurrence occurrence, Logger taskLogger) {
        EstimationTask estimationTask = getEstimationTask(occurrence);
        RelativePeriod relativePeriod = estimationTask.getPeriod().orElseGet(timeService::getAllRelativePeriod);
        QualityCodeSystem system = estimationTask.getQualityCodeSystem();

        estimationTask.getEndDeviceGroup().getMembers(occurrence.getTriggerTime()).stream()
                .filter(device -> device instanceof Meter)
                .map(Meter.class::cast)
                .filter(meter -> estimationService.getEstimationResolvers().stream().anyMatch(resolver -> resolver.isEstimationActive(meter)))
                .map(meter -> meter.getMeterActivation(occurrence.getTriggerTime()))
                .flatMap(Functions.asStream())
                .forEach(meterActivation -> estimationService.estimate(system, meterActivation,
                        period(meterActivation, relativePeriod, occurrence.getTriggerTime()), taskLogger));
        estimationTask.updateLastRun(occurrence.getTriggerTime());
    }

    private Range<Instant> period(MeterActivation meterActivation, RelativePeriod relativePeriod, Instant triggerTime) {
        ZonedDateTime referenceDate = ZonedDateTime.ofInstant(triggerTime, meterActivation.getChannelsContainer().getZoneId());
        return relativePeriod.getOpenClosedInterval(referenceDate);
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
