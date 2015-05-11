package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
        IReadingTypeExportTask task = occurrence.getTask();
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

        LoggingItemExporter loggingItemExporter = getItemExporter(dataFormatter, logger);

        catchingUnexpected(loggingExceptions(logger, () -> dataFormatter.startExport(occurrence, logger))).run();

        catchingUnexpected(() -> activeItems.forEach(item -> doProcess(loggingItemExporter, occurrence, item))).run();

        catchingUnexpected(loggingExceptions(logger, dataFormatter::endExport)).run();

        try (TransactionContext transactionContext = transactionService.getContext()) {
            activeItems.forEach(IReadingTypeDataExportItem::update);
            transactionContext.commit();
        }
    }

    private LoggingItemExporter getItemExporter(DataProcessor dataFormatter, Logger logger) {
        DefaultItemExporter defaultItemExporter = new DefaultItemExporter(dataFormatter);
        FatalExceptionGuardItemExporter exceptionGuardItemExporter = new FatalExceptionGuardItemExporter(defaultItemExporter);
        ItemExporter itemExporter = new TransactionItemExporter(transactionService, exceptionGuardItemExporter);
        return new LoggingItemExporter(thesaurus, transactionService, logger, itemExporter);
    }

    private LoggingExceptions loggingExceptions(Logger logger, Runnable runnable) {
        return new LoggingExceptions(runnable, logger);
    }

    private ExceptionsToFatallyFailed catchingUnexpected(Runnable decorated) {
        return new ExceptionsToFatallyFailed(decorated);
    }

    private Set<IReadingTypeDataExportItem> getActiveItems(IReadingTypeExportTask task, DataExportOccurrence occurrence) {
        return task.getEndDeviceGroup().getMembers(occurrence.getExportedDataInterval()).stream()
                .map(EndDeviceMembership::getEndDevice)
                .filter(device -> device instanceof Meter)
                .map(Meter.class::cast)
                .flatMap(meter -> readingTypeDataExportItems(task, meter))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private DataProcessor getDataProcessor(IReadingTypeExportTask task) {
        Map<String, Object> propertyMap = new HashMap<>();
        List<DataExportProperty> dataExportProperties = task.getDataExportProperties();
        DataProcessorFactory dataProcessorFactory = getDataProcessorFactory(task.getDataFormatter());
        for (DataExportProperty property : dataExportProperties) {
            propertyMap.put(property.getName(), property.useDefault() ? getDefaultValue(dataProcessorFactory, property) : property.getValue());
        }
        return dataProcessorFactory.createDataFormatter(propertyMap);
    }

    private Object getDefaultValue(DataProcessorFactory dataProcessorFactory, DataExportProperty property) {
        return dataProcessorFactory.getProperties().stream().filter(dep -> dep.getName().equals(property.getName()))
                .findFirst().orElseThrow(IllegalArgumentException::new).getPossibleValues().getDefault();
    }

    private DataProcessorFactory getDataProcessorFactory(String dataFormatter) {
        return dataExportService.getDataProcessorFactory(dataFormatter).orElseThrow(() -> new NoSuchDataProcessor(thesaurus, dataFormatter));
    }

    private void doProcess(ItemExporter itemExporter, DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        item.setLastRun(occurrence.getTriggerTime());
        try {
            itemExporter.exportItem(occurrence, item);
        } catch (DataExportException e) {
            // not fatal, we continue.
        }
    }

    private Stream<IReadingTypeDataExportItem> readingTypeDataExportItems(IReadingTypeExportTask task, Meter meter) {
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
