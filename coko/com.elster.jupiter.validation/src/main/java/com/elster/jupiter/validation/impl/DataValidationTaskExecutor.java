package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
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
        DataValidationOccurrence dataValidationOccurence = createOccurence(taskOccurrence);
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

    public DataValidationOccurrence createOccurence(TaskOccurrence taskOccurrence) {
        DataValidationOccurrence dataValidationOccurence = validationService.createValidationOccurrence(taskOccurrence);
        dataValidationOccurence.persist();
        return dataValidationOccurence;
    }

    private DataValidationOccurrence findOccurrence(TaskOccurrence occurrence) {
        return validationService.findDataValidationOccurrence(occurrence).orElseThrow(IllegalArgumentException::new);
    }

    private void doExecute(DataValidationOccurrence occurrence, Logger logger) {
        DataValidationTask task = occurrence.getTask();

        if (task.getEndDeviceGroup().isPresent()) {
            List<EndDevice> devices = task.getEndDeviceGroup().get().getMembers(Instant.now());
            for (EndDevice device : devices) {
                Optional<Meter> found = device.getAmrSystem().findMeter(device.getAmrId());
                if (found.isPresent()) {
                    List<? extends MeterActivation> activations = found.get().getMeterActivations();
                    for (MeterActivation activation : activations) {
                        try (TransactionContext transactionContext = transactionService.getContext()) {
                            validationService.validate(activation);
                            transactionContext.commit();
                        }
                        transactionService.execute(VoidTransaction.of(() -> MessageSeeds.TASK_VALIDATED_SUCCESFULLY.log(logger, thesaurus, device.getMRID(), occurrence.getStartDate().get())));
                    }

                }
            }
        } else if (task.getUsagePointGroup().isPresent()) {
            List<UsagePoint> usagePoints = task.getUsagePointGroup().get().getMembers(Instant.now());
            for (UsagePoint usagePoint : usagePoints) {
                List<? extends MeterActivation> activations = usagePoint.getMeterActivations();
                for (MeterActivation activation : activations) {
                    try (TransactionContext transactionContext = transactionService.getContext()) {
                        validationService.validate(activation);
                        transactionContext.commit();
                    }
                    transactionService.execute(VoidTransaction.of(() -> MessageSeeds.TASK_VALIDATED_SUCCESFULLY.log(logger, thesaurus, usagePoint.getMRID(), occurrence.getStartDate().get())));
                }

            }
        }

    }

    private LoggingExceptions loggingExceptions(Logger logger, Runnable runnable) {
        return new LoggingExceptions(runnable, logger);
    }

    private class LoggingExceptions implements Runnable {

        private final Runnable decorated;
        private final Logger logger;

        private LoggingExceptions(Runnable decorated, Logger logger) {
            this.decorated = decorated;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                decorated.run();
            } catch (RuntimeException e) {
                try (TransactionContext context = transactionService.getContext()) {
                    logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    context.commit();
                }
                throw e;
            }
        }
    }
}
