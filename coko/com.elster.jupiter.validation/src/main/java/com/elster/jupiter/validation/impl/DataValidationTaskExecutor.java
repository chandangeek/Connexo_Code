/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskStatus;
import com.elster.jupiter.validation.ValidationContextImpl;

import java.time.Clock;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

class DataValidationTaskExecutor implements TaskExecutor {

    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final ValidationServiceImpl validationService;
    private final ThreadPrincipalService threadPrincipalService;
    private final EventService eventService;
    private final Clock clock;
    private final User user;

    DataValidationTaskExecutor(ValidationServiceImpl validationService,
                               TransactionService transactionService,
                               Thesaurus thesaurus,
                               ThreadPrincipalService threadPrincipalService,
                               EventService eventService, Clock clock,
                               User user) {
        this.thesaurus = thesaurus;
        this.validationService = validationService;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
        this.eventService = eventService;
        this.clock = clock;
        this.user = user;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        createOccurrence(taskOccurrence);
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        threadPrincipalService.runAs(
                user,
                () -> runValidation(occurrence),
                Locale.getDefault()
        );

    }

    private void runValidation(TaskOccurrence occurrence) {
        DataValidationOccurrence dataValidationOccurrence = findOccurrence(occurrence);
        boolean success = false;
        String errorMessage = null;
        try {
            doExecute(dataValidationOccurrence, getLogger(occurrence));
            success = true;
        } catch (Exception ex) {
            errorMessage = ex.getLocalizedMessage();
            postFailEvent(eventService, occurrence, errorMessage);
            throw ex;
        } finally {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                dataValidationOccurrence = findAndLockOccurrence(occurrence); // Reload occurrence, because #doExecute can took a long time
                dataValidationOccurrence.end(success ? DataValidationTaskStatus.SUCCESS : DataValidationTaskStatus.FAILED, errorMessage);
                transactionContext.commit();
            }
        }
    }

    private Logger getLogger(TaskOccurrence occurrence) {
        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        return logger;
    }

    private DataValidationOccurrence createOccurrence(TaskOccurrence taskOccurrence) {
        return validationService.createValidationOccurrence(taskOccurrence);
    }

    private DataValidationOccurrence findOccurrence(TaskOccurrence occurrence) {
        return validationService.findDataValidationOccurrence(occurrence).orElseThrow(IllegalArgumentException::new);
    }

    private DataValidationOccurrence findAndLockOccurrence(TaskOccurrence occurrence) {
        return validationService.findAndLockDataValidationOccurrence(occurrence);
    }

    private void doExecute(DataValidationOccurrence occurrence, Logger logger) {
        DataValidationTask task = occurrence.getTask();
        switch (task.getQualityCodeSystem()) {
            case MDC:
                executeMdcTask(occurrence, logger, task);
                break;
            case MDM:
                executeMdmTask(occurrence, logger, task);
                break;
        }
    }

    private void executeMdcTask(DataValidationOccurrence occurrence, Logger logger, DataValidationTask task) {
        for (EndDevice device : task.getEndDeviceGroup().get().getMembers(clock.instant())) {
            device.getAmrSystem().findMeter(device.getAmrId()).ifPresent(meter -> {
                for (ChannelsContainer channelsContainer : meter.getChannelsContainers()) {
                    try (TransactionContext transactionContext = transactionService.getContext()) {
                        validationService.validate(new ValidationContextImpl(EnumSet.of(task.getQualityCodeSystem()), channelsContainer));
                        transactionContext.commit();
                    }
                }
                transactionService.execute(VoidTransaction.of(() ->
                        MessageSeeds.DEVICE_TASK_VALIDATED_SUCCESFULLY.log(logger, thesaurus, device.getName(), occurrence.getStartDate().get())));
            });
        }
    }

    private void executeMdmTask(DataValidationOccurrence occurrence, Logger logger, DataValidationTask task) {
        task.getUsagePointGroup().get().getMembers(clock.instant()).forEach(usagePoint -> {
            usagePoint.getEffectiveMetrologyConfigurations().forEach(effectiveMC -> {
                Stream<MetrologyContract> contractsStream = effectiveMC.getMetrologyConfiguration().getContracts().stream();
                task.getMetrologyPurpose()
                        .map(purpose -> contractsStream.filter(contract -> contract.getMetrologyPurpose().equals(purpose)))
                        .orElse(contractsStream)
                        .forEach(metrologyContract -> validate(EnumSet.of(task.getQualityCodeSystem()), metrologyContract, effectiveMC));
            });
            transactionService.execute(VoidTransaction.of(() ->
                    MessageSeeds.USAGE_POINT_TASK_VALIDATED_SUCCESFULLY.log(logger, thesaurus, usagePoint.getName(),
                            getTimeFormatter().format(occurrence.getStartDate().get()))));
        });
    }

    private DateTimeFormatter getTimeFormatter() {
        Locale locale = threadPrincipalService.getLocale();
        return DefaultDateTimeFormatters.longDate(locale).withLongTime().build().withZone(ZoneId.systemDefault()).withLocale(locale);
    }

    private void validate(Set<QualityCodeSystem> qualityCodeSystems, MetrologyContract metrologyContract,
                          EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration) {
        effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)
                .ifPresent(channelsContainer -> validate(qualityCodeSystems, channelsContainer, metrologyContract));
    }

    private void validate(Set<QualityCodeSystem> qualityCodeSystems, ChannelsContainer channelsContainer, MetrologyContract metrologyContract) {
        try (TransactionContext transactionContext = transactionService.getContext()) {
            validationService.validate(new ValidationContextImpl(qualityCodeSystems, channelsContainer, metrologyContract));
            transactionContext.commit();
        }
    }
}
