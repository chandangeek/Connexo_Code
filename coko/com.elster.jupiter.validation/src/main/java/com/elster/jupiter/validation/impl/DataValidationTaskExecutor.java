package com.elster.jupiter.validation.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.DataValidationOccurence;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationService;

import java.util.logging.Level;
import java.util.logging.Logger;


public class DataValidationTaskExecutor implements TaskExecutor {

    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final ValidationService validationService;

    public DataValidationTaskExecutor(ValidationService validationService,TransactionService transactionService, Thesaurus thesaurus){
        this.thesaurus = thesaurus;
        this.validationService = validationService;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        DataValidationOccurence dataValidationOccurence = createOccurence(taskOccurrence);

    }

    private Logger getLogger(TaskOccurrence occurrence) {
        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        return logger;
    }

    public DataValidationOccurence createOccurence(TaskOccurrence taskOccurrence){
        DataValidationOccurence dataValidationOccurence= validationService.createValidationOccurrence(taskOccurrence);
        dataValidationOccurence.persist();
        return dataValidationOccurence;
    }

    private DataValidationOccurence findOccurrence(TaskOccurrence occurrence) {
        return validationService.findDataValidationOccurrence(occurrence).orElseThrow(IllegalArgumentException::new);
    }

    private void doExecute(DataValidationOccurence occurrence, Logger logger) {
        DataValidationTask task = occurrence.getTask();

//        implement DataValidationTaskItem

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
