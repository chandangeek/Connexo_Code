package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.*;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DataExportTaskExecutor implements TaskExecutor {

    private final IDataExportService dataExportService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    public DataExportTaskExecutor(IDataExportService dataExportService, TransactionService transactionService, Thesaurus thesaurus) {
        this.dataExportService = dataExportService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        IDataExportOccurrence dataExportOccurrence = createOccurrence(occurrence);
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        IDataExportOccurrence dataExportOccurrence = findOccurrence(occurrence);
        boolean success = false;
        String errorMessage = null;
        try {
            doExecute(dataExportOccurrence, getLogger(occurrence));
            success = true;
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
            throw ex;
        } finally {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                dataExportOccurrence.end(success ? DataExportStatus.SUCCESS : DataExportStatus.FAILED, errorMessage);
                dataExportOccurrence.update();
                transactionContext.commit();
            }
        }

    }

    private Logger getLogger(TaskOccurrence occurrence) {
        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        return logger;
    }

    private IDataExportOccurrence createOccurrence(TaskOccurrence occurrence) {
        IDataExportOccurrence dataExportOccurrence = dataExportService.createExportOccurrence(occurrence);
        dataExportOccurrence.persist();
        return dataExportOccurrence;
    }

    private IDataExportOccurrence findOccurrence(TaskOccurrence occurrence) {
        return dataExportService.findDataExportOccurrence(occurrence).orElseThrow(IllegalArgumentException::new);
    }

    private void doExecute(IDataExportOccurrence occurrence, Logger logger) {
        IReadingTypeDataExportTask task = occurrence.getTask();
        Set<IReadingTypeDataExportItem> activeItems;
        try (TransactionContext context = transactionService.getContext()) {
            activeItems = getActiveItems(task, occurrence);

            task.getExportItems().stream()
                    .filter(item -> !activeItems.contains(item))
                    .peek(IReadingTypeDataExportItem::deactivate)
                    .forEach(IReadingTypeDataExportItem::update);
            activeItems.stream()
                    .peek(IReadingTypeDataExportItem::activate)
                    .forEach(IReadingTypeDataExportItem::update);
            context.commit();
        }

        DataProcessor dataFormatter = getDataProcessor(task);

        DefaultItemExporter defaultItemExporter = new DefaultItemExporter(dataFormatter);
        FatalExceptionGuardItemExporter exceptionGuardItemExporter = new FatalExceptionGuardItemExporter(defaultItemExporter);
        ItemExporter itemExporter = new TransactionItemExporter(transactionService, exceptionGuardItemExporter);
        LoggingItemExporter loggingItemExporter = new LoggingItemExporter(thesaurus, transactionService, logger, itemExporter);

        catchingUnexpected(loggingExceptions(logger, () -> dataFormatter.startExport(occurrence, logger))).run();

        catchingUnexpected(() -> activeItems.forEach(item -> doProcess(loggingItemExporter, occurrence, item))).run();

        catchingUnexpected(loggingExceptions(logger, dataFormatter::endExport)).run();

        try (TransactionContext transactionContext = transactionService.getContext()) {
            activeItems.forEach(IReadingTypeDataExportItem::update);
            transactionContext.commit();
        }
    }

    private LoggingExceptions loggingExceptions(Logger logger, Runnable runnable) {
        return new LoggingExceptions(runnable, logger);
    }

    private ExceptionsToFatallyFailed catchingUnexpected(Runnable decorated) {
        return new ExceptionsToFatallyFailed(decorated);
    }

    private Set<IReadingTypeDataExportItem> getActiveItems(IReadingTypeDataExportTask task, DataExportOccurrence occurrence) {
        return task.getEndDeviceGroup().getMembers(occurrence.getExportedDataInterval()).stream()
                .map(EndDeviceMembership::getEndDevice)
                .filter(device -> device instanceof Meter)
                .map(Meter.class::cast)
                .flatMap(meter -> readingTypeDataExportItems(task, meter))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private DataProcessor getDataProcessor(IReadingTypeDataExportTask task) {
        List<DataExportProperty> dataExportProperties = task.getDataExportProperties();
        DataProcessorFactory dataProcessorFactory = getDataProcessorFactory(task.getDataFormatter());
        for(PropertySpec<?> propertySpec : dataProcessorFactory.getProperties()) {
            if (!dataExportProperties.stream().anyMatch(dep -> dep.getName().equals(propertySpec.getName()))) {
                dataExportProperties.add(new DataExportProperty() {
                    @Override
                    public ReadingTypeDataExportTask getTask() {
                        return task;
                    }

                    @Override
                    public String getDisplayName() {
                        return propertySpec.getName();
                    }

                    @Override
                    public Object getValue() {
                        return propertySpec.getPossibleValues().getDefault();
                    }

                    @Override
                    public void setValue(Object value) {

                    }

                    @Override
                    public void save() {

                    }

                    @Override
                    public String getName() {
                        return propertySpec.getName();
                    }
                }) ;
            }
        }
        return dataProcessorFactory.createDataFormatter(dataExportProperties);
    }

    private DataProcessorFactory getDataProcessorFactory(String dataFormatter) {
        return dataExportService.getDataProcessorFactory(dataFormatter).orElseThrow(() -> new NoSuchDataProcessor(thesaurus ,dataFormatter));
    }

    private void doProcess(ItemExporter itemExporter, DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        item.setLastRun(occurrence.getTriggerTime());
        try {
            itemExporter.exportItem(occurrence, item);
        } catch (DataExportException e) {
            // not fatal, we continue.
        }
    }

    private Stream<IReadingTypeDataExportItem> readingTypeDataExportItems(IReadingTypeDataExportTask task, Meter meter) {
        return task.getReadingTypes().stream()
                .map(r -> task.getExportItems().stream()
                                .map(IReadingTypeDataExportItem.class::cast)
                                .filter(item -> r.equals(item.getReadingType()))
                                .filter(i -> i.getReadingContainer().is(meter))
                                .findAny()
                                .orElseGet(() -> task.addExportItem(meter, r))
                );
    }

    private static class ExceptionsToFatallyFailed implements Runnable {

        private final Runnable decorated;

        private ExceptionsToFatallyFailed(Runnable decorated) {
            this.decorated = decorated;
        }

        @Override
        public void run() {
            try {
                decorated.run();
            } catch (FatalDataExportException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new FatalDataExportException(e);
            }
        }
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
