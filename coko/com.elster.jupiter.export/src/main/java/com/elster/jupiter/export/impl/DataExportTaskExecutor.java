package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.NoSuchDataProcessorException;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.util.LinkedHashSet;
import java.util.Set;
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
            occurrence.start();
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
        TransactionItemExporter transactionItemExporter = new TransactionItemExporter(transactionService, defaultItemExporter);
        ItemExporter itemExporter = new LoggingItemExporter(thesaurus, logger, transactionItemExporter);

        try {
            dataFormatter.startExport(occurrence, logger);

            activeItems.forEach(item -> doProcess(itemExporter, occurrence, item));

            dataFormatter.endExport();
        } catch (RuntimeException e) {
            throw new FatalDataExportException(e);
        }

        try (TransactionContext transactionContext = transactionService.getContext()) {
            activeItems.forEach(IReadingTypeDataExportItem::update);
            transactionContext.commit();
        }
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
        DataProcessorFactory dataProcessorFactory = dataExportService.getDataProcessorFactory(task.getDataFormatter()).orElseThrow(NoSuchDataProcessorException::new);

        return dataProcessorFactory.createDataFormatter(task.getDataExportProperties());
    }

    private void doProcess(ItemExporter itemExporter, DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        item.setLastRun(occurrence.getTriggerTime());
        itemExporter.exportItem(occurrence, item);
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

}
