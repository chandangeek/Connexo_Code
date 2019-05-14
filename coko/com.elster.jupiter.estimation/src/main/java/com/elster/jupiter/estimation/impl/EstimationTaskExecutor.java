/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

class EstimationTaskExecutor implements TaskExecutor {

    private final IEstimationService estimationService;
    private final ValidationService validationService;
    private final MeteringService meteringService;
    private final TimeService timeService;

    private final TransactionService transactionService;
    private final EventService eventService;
    private final ThreadPrincipalService threadPrincipalService;
    private final User user;

    public EstimationTaskExecutor(IEstimationService estimationService, ValidationService validationService,
                                  MeteringService meteringService, TimeService timeService,
                                  TransactionService transactionService, EventService eventService, ThreadPrincipalService threadPrincipalService, User user) {
        this.estimationService = estimationService;
        this.validationService = validationService;
        this.meteringService = meteringService;
        this.timeService = timeService;
        this.transactionService = transactionService;
        this.eventService = eventService;
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
                postFailEvent(eventService, occurrence, e.getLocalizedMessage());
                transactionService.run(() -> loggingContext.severe(taskLogger, e));
            } finally {
                transactionService.run(() -> getEstimationTask(occurrence).updateLastRun(occurrence.getTriggerTime()));
            }
        }
    }

    private void tryExecute(TaskOccurrence occurrence, Logger taskLogger) {
        EstimationTask estimationTask = getEstimationTask(occurrence);
        RelativePeriod relativePeriod = estimationTask.getPeriod().orElseGet(timeService::getAllRelativePeriod);
        Instant triggerTime = occurrence.getTriggerTime();
        boolean shouldRevalidate = estimationTask.shouldRevalidate();

        estimationTask.getEndDeviceGroup().ifPresent(endDeviceGroup ->
                estimateDevices(endDeviceGroup, relativePeriod, triggerTime, shouldRevalidate, taskLogger)
        );
        estimationTask.getUsagePointGroup().ifPresent(usagePointGroup ->
                estimateUsagePoints(usagePointGroup, estimationTask.getMetrologyPurpose().orElse(null), relativePeriod, triggerTime, shouldRevalidate, taskLogger)
        );
    }

    private void estimateDevices(EndDeviceGroup endDeviceGroup, RelativePeriod relativePeriod, Instant triggerTime, boolean revalidate, Logger taskLogger) {
        QualityCodeSystem system = QualityCodeSystem.MDC;
        Range<Instant> interval = relativePeriod.getOpenClosedInterval(ZonedDateTime.ofInstant(triggerTime, ZoneId.systemDefault()));
        meteringService.getMeterWithReadingQualitiesQuery(interval, ReadingQualityType.of(system, QualityCodeIndex.SUSPECT))
                .select(ListOperator.IN.contains(endDeviceGroup.toSubQuery("id"), "id")).stream()
                .filter(meter -> estimationService.getEstimationResolvers().stream().anyMatch(resolver -> resolver.isEstimationActive(meter)))
                .map(meter -> meter.getMeterActivation(triggerTime))
                .flatMap(Functions.asStream())
                .map(MeterActivation::getChannelsContainer)
                .forEach(channelsContainer -> doEstimateTransactional(system, channelsContainer, relativePeriod, triggerTime, revalidate, taskLogger));
    }

    private void estimateUsagePoints(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose, RelativePeriod relativePeriod,
                                     Instant triggerTime, boolean revalidate, Logger taskLogger) {
        QualityCodeSystem system = QualityCodeSystem.MDM;
        Condition condition = ListOperator.IN.contains(usagePointGroup.toSubQuery("id"), "effectiveMetrologyContract.metrologyConfiguration.usagePoint");
        if (metrologyPurpose != null) {
            condition = condition.and(where("effectiveMetrologyContract.metrologyContract.metrologyPurpose").isEqualTo(metrologyPurpose));
        }
        Range<Instant> interval = relativePeriod.getOpenClosedInterval(ZonedDateTime.ofInstant(triggerTime, ZoneId.systemDefault()));
        meteringService.getChannelsContainerWithReadingQualitiesQuery(interval, ReadingQualityType.of(system, QualityCodeIndex.SUSPECT))
                .select(condition).stream()
                .distinct()
                .forEach(channelsContainer -> doEstimateTransactional(system, channelsContainer, relativePeriod, triggerTime, revalidate, taskLogger));
    }

    private void doEstimateTransactional(QualityCodeSystem system, ChannelsContainer channelsContainer, RelativePeriod relativePeriod,
                                         Instant triggerTime, boolean revalidate, Logger taskLogger) {
        try (TransactionContext transactionContext = transactionService.getContext()) {
            Range<Instant> period = period(relativePeriod, triggerTime, channelsContainer.getZoneId());
            EstimationReport estimationReport = estimationService.estimate(system, channelsContainer, period, taskLogger);
            if (revalidate) {
                revalidate(system, estimationReport, taskLogger);
            }
            transactionContext.commit();
        } catch (Exception e) {
            String estimatedObjectName = getEstimatedObjectName(channelsContainer);
            transactionService.run(() -> taskLogger.log(Level.WARNING, "Failed to estimate " + estimatedObjectName + ". Error: " + e.getLocalizedMessage(), e));
        }
    }

    private String getEstimatedObjectName(ChannelsContainer channelsContainer) {
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            return getEstimatedObjectName((MetrologyContractChannelsContainer) channelsContainer);
        } else {
            return channelsContainer.getMeter().map(IdentifiedObject::getName).get();
        }
    }

    private String getEstimatedObjectName(MetrologyContractChannelsContainer metrologyContractChannelsContainer) {
        return metrologyContractChannelsContainer.getUsagePoint().map(IdentifiedObject::getName).get()
                + "/" + metrologyContractChannelsContainer.getMetrologyContract().getMetrologyPurpose().getName();
    }

    private void revalidate(QualityCodeSystem system, EstimationReport estimationReport, Logger taskLogger) {
        Map<Channel, List<EstimationResult>> estimationResultsPerChannel =
                estimationReport.getResults().values().stream()
                        .filter(estimationResult -> !estimationResult.estimated().isEmpty())
                        .collect(Collectors.groupingBy(estimationResult -> estimationResult.estimated().get(0).getChannel()));
        estimationResultsPerChannel.forEach((channel, estimationResult) -> {
            Instant firstEstimatedOnChannel = estimationResult.stream()
                    .map(EstimationResult::estimated)
                    .flatMap(Collection::stream)
                    .map(EstimationBlock::estimatables)
                    .flatMap(Collection::stream)
                    .map(Estimatable::getTimestamp)
                    .min(Comparator.naturalOrder())
                    .get();
            Instant lastChecked = validationService.getLastChecked(channel).orElse(null);
            taskLogger.log(Level.INFO, "Re-validation of estimated readings on "
                    + getEstimatedObjectName(channel.getChannelsContainer()) + "/" + channel.getMainReadingType().getFullAliasName());
            validationService.validate(
                    new ValidationContextImpl(ImmutableSet.of(system), channel.getChannelsContainer(), channel.getMainReadingType()),
                    Ranges.closed(firstEstimatedOnChannel, lastChecked)
            );
        });
    }

    private Range<Instant> period(RelativePeriod relativePeriod, Instant triggerTime, ZoneId zoneId) {
        ZonedDateTime referenceDate = ZonedDateTime.ofInstant(triggerTime, zoneId);
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
}
