package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskStatus;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public class DataValidationTaskExecutor implements TaskExecutor {

    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final ValidationServiceImpl validationService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ThreadPrincipalService threadPrincipalService;
    private final User user;


    public DataValidationTaskExecutor(ValidationServiceImpl validationService, MetrologyConfigurationService metrologyConfigurationService, TransactionService transactionService, Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService, User user) {
        this.thesaurus = thesaurus;
        this.validationService = validationService;
        this.transactionService = transactionService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.threadPrincipalService = threadPrincipalService;
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
            errorMessage = ex.getMessage();
            throw ex;
        } finally {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                dataValidationOccurrence = findAndLockOccurrence(occurrence); // Reload occurrence, because #doExecute can took a long time
                dataValidationOccurrence.end(success ? DataValidationTaskStatus.SUCCESS : DataValidationTaskStatus.FAILED, errorMessage);
                dataValidationOccurrence.update();
                transactionContext.commit();
            }
        }
    }

    private Logger getLogger(TaskOccurrence occurrence) {
        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        return logger;
    }

    public DataValidationOccurrence createOccurrence(TaskOccurrence taskOccurrence) {
        DataValidationOccurrence dataValidationOccurrence = validationService.createValidationOccurrence(taskOccurrence);
        dataValidationOccurrence.persist();
        return dataValidationOccurrence;
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
        List<EndDevice> devices = task.getEndDeviceGroup().get().getMembers(Instant.now());
        for (EndDevice device : devices) {
            Optional<Meter> found = device.getAmrSystem().findMeter(device.getAmrId());
            if (found.isPresent()) {
                List<ChannelsContainer> channelsContainers = found.get().getChannelsContainers();
                for (ChannelsContainer channelsContainer : channelsContainers) {
                    try (TransactionContext transactionContext = transactionService.getContext()) {
                        validationService.validate(new ValidationContextImpl(EnumSet.of(task.getQualityCodeSystem()), channelsContainer));
                        transactionContext.commit();
                    }
                    transactionService.execute(VoidTransaction.of(() -> MessageSeeds.DEVICE_TASK_VALIDATED_SUCCESFULLY.log(logger, thesaurus, device.getMRID(), occurrence.getStartDate()
                            .get())));
                }
            }
        }
    }

    private void executeMdmTask(DataValidationOccurrence occurrence, Logger logger, DataValidationTask task) {
        MetrologyContract metrologyContract = task.getMetrologyContract().get();
        metrologyConfigurationService.getEffectiveMetrologyConfigurationFinderFor(metrologyContract).stream()
                .forEach(effectiveMetrologyConfiguration -> {
                    // Validate inputs provided by linked meters
                    validateUsagePointInputs(EnumSet.of(task.getQualityCodeSystem()), metrologyContract, effectiveMetrologyConfiguration);
                    // Validate outputs provided by metrology configuration
                    validateUsagePointOutputs(EnumSet.of(task.getQualityCodeSystem()), metrologyContract, effectiveMetrologyConfiguration);
                    transactionService.execute(VoidTransaction.of(() -> MessageSeeds.USAGE_POINT_TASK_VALIDATED_SUCCESFULLY.log(logger, thesaurus, effectiveMetrologyConfiguration.getUsagePoint()
                            .getMRID(), occurrence.getStartDate().get())));
                });
    }

    private void validateUsagePointInputs(Set<QualityCodeSystem> qualityCodeSystems, MetrologyContract metrologyContract, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration) {
        effectiveMetrologyConfiguration.getUsagePoint().getCurrentMeterActivations()
                .stream()
                .map(MeterActivation::getChannelsContainer)
                .forEach(channelsContainer -> {
                    try (TransactionContext transactionContext = transactionService.getContext()) {
                        validationService.validate(new ValidationContextImpl(qualityCodeSystems, channelsContainer).setMetrologyContract(metrologyContract));
                        transactionContext.commit();
                    }
                });
    }

    private void validateUsagePointOutputs(Set<QualityCodeSystem> qualityCodeSystems, MetrologyContract metrologyContract, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration) {
        effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).ifPresent(channelsContainer -> {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                validationService.validate(new ValidationContextImpl(qualityCodeSystems, channelsContainer).setMetrologyContract(metrologyContract));
                transactionContext.commit();
            }
        });
    }
}
