package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointFilter;
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
import com.elster.jupiter.validation.ValidationService;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

public class DataValidationTaskExecutor implements TaskExecutor {

    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final ValidationService validationService;
    private final MeteringService meteringService;
    private final ThreadPrincipalService threadPrincipalService;
    private final User user;


    public DataValidationTaskExecutor(ValidationService validationService, MeteringService meteringService, TransactionService transactionService, Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService, User user) {
        this.thesaurus = thesaurus;
        this.validationService = validationService;
        this.transactionService = transactionService;
        this.meteringService = meteringService;
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
        return ((ValidationServiceImpl) validationService).findAndLockDataValidationOccurrence(occurrence);
    }

    private void doExecute(DataValidationOccurrence occurrence, Logger logger) {
        DataValidationTask task = occurrence.getTask();

        switch (task.getQualityCodeSystem()) {
            case MDC:
                List<EndDevice> devices = task.getEndDeviceGroup().get().getMembers(Instant.now());
                for (EndDevice device : devices) {
                    Optional<Meter> found = device.getAmrSystem().findMeter(device.getAmrId());
                    if (found.isPresent()) {
                        List<ChannelsContainer> channelsContainers = found.get().getChannelsContainers();
                        for (ChannelsContainer channelsContainer : channelsContainers) {
                            try (TransactionContext transactionContext = transactionService.getContext()) {
                                validationService.validate(EnumSet.of(task.getQualityCodeSystem()), channelsContainer);
                                transactionContext.commit();
                            }
                            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.DEVICE_TASK_VALIDATED_SUCCESFULLY.log(logger, thesaurus, device.getMRID(), occurrence.getStartDate()
                                    .get())));
                        }

                    }
                }
                break;
            case MDM:
                MetrologyContract metrologyContract = task.getMetrologyContract().get();
                UsagePointFilter usagePointFilter = new UsagePointFilter();
                usagePointFilter.setMetrologyContract(metrologyContract);
                meteringService.getUsagePoints(usagePointFilter).stream()
                        .map(UsagePoint::getEffectiveMetrologyConfiguration)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(effectiveMetrologyConfiguration -> {
                            // Validate inputs
                            effectiveMetrologyConfiguration.getUsagePoint().getCurrentMeterActivations()
                                    .stream()
                                    .map(MeterActivation::getChannelsContainer)
                                    .forEach(channelsContainer -> {
                                        try (TransactionContext transactionContext = transactionService.getContext()) {
                                            validationService.validate(EnumSet.of(task.getQualityCodeSystem()), MetrologyContractChannelsContainerWrapper.from(channelsContainer, metrologyContract));
                                            transactionContext.commit();
                                        }
                                    });
                            // Validate outputs
                            effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).ifPresent(channelsContainer -> {
                                try (TransactionContext transactionContext = transactionService.getContext()) {
                                    validationService.validate(EnumSet.of(task.getQualityCodeSystem()), channelsContainer);
                                    transactionContext.commit();
                                }
                            });
                            transactionService.execute(VoidTransaction.of(() -> MessageSeeds.USAGE_POINT_TASK_VALIDATED_SUCCESFULLY.log(logger, thesaurus, effectiveMetrologyConfiguration.getUsagePoint()
                                    .getMRID(), occurrence.getStartDate().get())));
                        });
                break;
        }
    }
}
