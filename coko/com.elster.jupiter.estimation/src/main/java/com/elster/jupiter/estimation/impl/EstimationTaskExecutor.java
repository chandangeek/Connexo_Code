/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

class EstimationTaskExecutor implements TaskExecutor {

    private final IEstimationService estimationService;
    private final TransactionService transactionService;
    private final MeteringService meteringService;
    private final TimeService timeService;
    private final ThreadPrincipalService threadPrincipalService;
    private final User user;

    public EstimationTaskExecutor(IEstimationService estimationService, TransactionService transactionService, MeteringService meteringService, TimeService timeService,
                                  ThreadPrincipalService threadPrincipalService, User user) {
        this.estimationService = estimationService;
        this.transactionService = transactionService;
        this.meteringService = meteringService;
        this.timeService = timeService;
        this.threadPrincipalService = threadPrincipalService;
        this.user = user;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        threadPrincipalService.runAs(user, () -> runEstimation(occurrence), Locale.getDefault());
    }

    private void runEstimation(TaskOccurrence occurrence) {
        try (LoggingContext loggingContext = LoggingContext.getCloseableContext()) {
            Logger taskLogger = createTaskLogger(occurrence);
            try {
                tryExecute(occurrence, taskLogger);
            } catch (Exception e) {
                transactionService.run(() -> loggingContext.severe(taskLogger, e));
            } finally {
                transactionService.run(() -> getEstimationTask(occurrence).updateLastRun(occurrence.getTriggerTime()));
            }
        }
    }

    private void tryExecute(TaskOccurrence occurrence, Logger taskLogger) {
        EstimationTask estimationTask = getEstimationTask(occurrence);
        RelativePeriod relativePeriod = estimationTask.getPeriod().orElseGet(timeService::getAllRelativePeriod);
        QualityCodeSystem system = estimationTask.getQualityCodeSystem();

        if(estimationTask.getEndDeviceGroup().isPresent()) {
            meteringService.getMeterWithReadingQualitiesQuery(relativePeriod.getOpenClosedInterval(ZonedDateTime.ofInstant(occurrence
                    .getTriggerTime(), ZoneId.systemDefault())), ReadingQualityType.of(system, QualityCodeIndex.SUSPECT))
                    .select(ListOperator.IN.contains(estimationTask.getEndDeviceGroup().get().toSubQuery("id"), "id"))
                    .stream()
                    .filter(meter -> estimationService.getEstimationResolvers()
                            .stream()
                            .anyMatch(resolver -> resolver.isEstimationActive(meter)))
                    .map(meter -> meter.getMeterActivation(occurrence.getTriggerTime()))
                    .flatMap(Functions.asStream())
                    .forEach(meterActivation -> doEstimateTransactional(meterActivation, system, relativePeriod, occurrence
                            .getTriggerTime(), taskLogger));
        } else if (estimationTask.getUsagePointGroup().isPresent()) {
            Condition condition = ListOperator.IN.contains(estimationTask.getUsagePointGroup().get().toSubQuery("id"),
                    "effectiveMetrologyContract.metrologyConfiguration.usagePoint");
            if (estimationTask.getMetrologyPurpose().isPresent()) {
                condition = condition.and(Where.where("effectiveMetrologyContract.metrologyContract.metrologyPurpose")
                        .isEqualTo(estimationTask.getMetrologyPurpose().get()));
            }
            getChannelsContainersQuery(relativePeriod, occurrence, system).select(condition).stream()
                    .distinct()
                    .forEach(channelsContainer -> doEstimateTransactional(occurrence, channelsContainer, system, relativePeriod, taskLogger));
        }
    }

    private void doEstimateTransactional(MeterActivation meterActivation, QualityCodeSystem system, RelativePeriod relativePeriod, Instant triggerTime, Logger taskLogger) {
        try {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                estimationService.estimate(system, meterActivation.getChannelsContainer(), period(meterActivation.getChannelsContainer(), relativePeriod, triggerTime), taskLogger);
                transactionContext.commit();
            }
        } catch (Exception ex) {
            transactionService.run(() -> taskLogger.log(Level.WARNING, "Failed to estimate " + meterActivation.getMeter().map(IdentifiedObject::getName)
                    .orElseGet(() -> meterActivation.getUsagePoint(triggerTime).map(IdentifiedObject::getName).orElse("Unknown"))
                    + " . Error: " + ex.getLocalizedMessage(), ex ));
        }
    }

    private void doEstimateTransactional(TaskOccurrence occurrence, ChannelsContainer channelsContainer, QualityCodeSystem system, RelativePeriod relativePeriod, Logger taskLogger) {
        try {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                estimationService.estimate(system, channelsContainer, period(channelsContainer, relativePeriod, occurrence.getTriggerTime()), taskLogger);
                transactionContext.commit();
            }
        } catch (Exception ex) {
            transactionService.run(() -> taskLogger.log(Level.WARNING, "Failed to estimate "
                    + channelsContainer.getUsagePoint().map(IdentifiedObject::getName).orElse("Unknown")
                    + " . Error: " + ex.getLocalizedMessage(), ex ));
        }
    }

    private Range<Instant> period(ChannelsContainer channelsContainer, RelativePeriod relativePeriod, Instant triggerTime) {
        ZonedDateTime referenceDate = ZonedDateTime.ofInstant(triggerTime, channelsContainer.getZoneId());
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

    private Query<ChannelsContainer> getChannelsContainersQuery(RelativePeriod relativePeriod, TaskOccurrence occurrence, QualityCodeSystem system) {
        return meteringService.getChannelsContainerWithReadingQualitiesQuery(
                relativePeriod.getOpenClosedInterval(
                        ZonedDateTime.ofInstant(occurrence.getTriggerTime(), ZoneId.systemDefault())), ReadingQualityType.of(system, QualityCodeIndex.SUSPECT));
    }
}
