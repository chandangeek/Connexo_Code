/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.SimpleFormattedData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Checks.is;

class DataExportTaskExecutor implements TaskExecutor {

    private final IDataExportService dataExportService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final LocalFileWriter localFileWriter;
    private final Clock clock;
    private final ThreadPrincipalService threadPrincipalService;
    private final EventService eventService;

    DataExportTaskExecutor(IDataExportService dataExportService,
                           TransactionService transactionService,
                           LocalFileWriter localFileWriter,
                           Thesaurus thesaurus,
                           Clock clock,
                           ThreadPrincipalService threadPrincipalService, EventService eventService) {
        this.dataExportService = dataExportService;
        this.transactionService = transactionService;
        this.localFileWriter = localFileWriter;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.threadPrincipalService = threadPrincipalService;
        this.eventService = eventService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        try {
            createOccurrence(occurrence);
        } catch (Exception e) {
            postFailEvent(eventService, occurrence, e.getLocalizedMessage());
            throw e;
        }
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        boolean success = false;
        String errorMessage = null;
        Exception thrown = null;

        Logger occurrenceLogger = occurrence.getRetryTime().isPresent() ?
                getLogger(occurrence, occurrence.getRecurrentTask().getHistory().getVersionAt(occurrence.getRetryTime().get()).get()) :
                getLogger(occurrence, occurrence.getRecurrentTask());
        try {
            catchingUnexpected(() -> doExecute(findOccurrence(occurrence), occurrenceLogger)).run();
            success = true;
        } catch (Exception ex) {
            thrown = ex;
            errorMessage = ex.getLocalizedMessage();
            if (is(errorMessage).emptyOrOnlyWhiteSpace()) {
                errorMessage = ex.toString();
            }
            postFailEvent(eventService, occurrence, errorMessage);
            throw ex;
        } finally {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                //Refetch dataExportOccurrence to avoid Optimistic Lock exceptions
                IDataExportOccurrence dataExportOccurrence = findOccurrence(occurrence);
                if (thrown != null) {
                    if (thrown.getCause() instanceof DestinationFailedException) {
                        occurrenceLogger.log(Level.SEVERE, errorMessage, thrown);
                    } else {
                        //default error message (fallback)
                        MessageSeeds.DEFAULT_MESSAGE_EXPORT_FAILED.log(occurrenceLogger, thesaurus, thrown.getLocalizedMessage());
                    }
                }
                dataExportOccurrence.end(success ? DataExportStatus.SUCCESS : DataExportStatus.FAILED, errorMessage);
                dataExportOccurrence.update();
                transactionContext.commit();
            }
        }
    }

    private Logger getLogger(TaskOccurrence occurrence, RecurrentTask recurrentTask) {
        Logger logger = Logger.getAnonymousLogger();
        logger.addHandler(occurrence.createTaskLogHandler(recurrentTask).asHandler());
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
        IExportTask task = occurrence.getTask();

        Stream<ExportData> data = getDataSelector(task, logger, occurrence).selectData(occurrence);
        if (task.hasDefaultSelector() && task.getReadingDataSelectorConfig().isPresent()) {
            try (TransactionContext context = transactionService.getContext()) {
                task.getReadingDataSelectorConfig().get().getActiveItems(occurrence).stream()
                        .peek(item -> item.setLastRun(occurrence.getTriggerTime()))
                        .forEach(ReadingTypeDataExportItem::update);
                context.commit();
            }
        }

        DataFormatter dataFormatter = getDataFormatter(task, occurrence);
        loggingExceptions(logger, () -> dataFormatter.startExport(occurrence, logger)).run();
        ItemExporter itemExporter = new LazyItemExporter(dataFormatter, logger);

        CompositeDataExportDestination destination = occurrence.getRetryTime()
                .map(task::getCompositeDestination)
                .orElseGet(task::getCompositeDestination);
        List<ExportData> dataList;
        Map<StructureMarker, Path> files;
        if (destination.hasDataDestinations()) {
            dataList = data.collect(Collectors.toList());
            data = dataList.stream();
        } else {
            dataList = Collections.emptyList();
        }
        if (destination.hasFileDestinations()) {
            FormattedData formattedData = task.hasDefaultSelector() && task.getReadingDataSelectorConfig().isPresent() ?
                    doProcessFromDefaultReadingSelector(occurrence, data, itemExporter) :
                    dataFormatter.processData(data);
            files = localFileWriter.writeToTempFiles(formattedData.getData());
        } else {
            files = Collections.emptyMap();
        }
        DataSendingStatus dataSendingStatus = destination.send(dataList, files, new TagReplacerFactoryForOccurrence(occurrence), logger, thesaurus);

        itemExporter.done();

        loggingExceptions(logger, dataFormatter::endExport).run();

        if (task.hasDefaultSelector() && task.getReadingDataSelectorConfig().isPresent()) {
            try (TransactionContext context = transactionService.getContext()) {
                task.getReadingDataSelectorConfig().get().getActiveItems(occurrence).stream()
                        .filter(item -> {
                            boolean needToUpdate = false;
                            if (!dataSendingStatus.isFailedForNewData(item) && !item.isExportPostponedForNewData()) {
                                // move lastExportedNewData not to send these data as 'new' anymore
                                // if we move lastExportedChangedData as well, unsent changed data will be lost next time
                                occurrence.getDefaultSelectorOccurrence().ifPresent(s -> item.setLastExportedNewData(s.getExportedDataInterval().upperEndpoint()));
                                needToUpdate = true;
                            }
                            if (!dataSendingStatus.isFailedForChangedData(item) && !item.isExportPostponedForChangedData()) {
                                // move lastExportedChangedData not to send these changed data anymore
                                // if we move lastExportedNewData as well, unsent new data will be lost next time
                                item.setLastExportedChangedData(occurrence.getTriggerTime());
                                needToUpdate = true;
                            }
                            return needToUpdate;
                        })
                        .forEach(ReadingTypeDataExportItem::update);
                context.commit();
            }
        }
        dataSendingStatus.throwExceptionIfFailed(thesaurus);
    }

    private LoggingItemExporter getItemExporter(DataFormatter dataFormatter, Logger logger) {
        DefaultItemExporter defaultItemExporter = new DefaultItemExporter(dataFormatter);
        FatalExceptionGuardItemExporter exceptionGuardItemExporter = new FatalExceptionGuardItemExporter(defaultItemExporter);
        ItemExporter itemExporter = new TransactionItemExporter(transactionService, exceptionGuardItemExporter);
        return new LoggingItemExporter(thesaurus, transactionService, logger, itemExporter, threadPrincipalService);
    }

    private LoggingExceptions loggingExceptions(Logger logger, Runnable runnable) {
        return new LoggingExceptions(runnable, logger);
    }

    private ExceptionsToFatallyFailed catchingUnexpected(Runnable decorated) {
        return new ExceptionsToFatallyFailed(decorated);
    }

    private DataFormatter getDataFormatter(IExportTask task, IDataExportOccurrence occurrence) {
        Instant at = occurrence.getRetryTime().orElse(occurrence.getTriggerTime());
        List<DataExportProperty> dataExportProperties = task.getDataExportProperties(at);
        DataFormatterFactory dataFormatterFactory = task.getDataFormatterFactory();
        Map<String, Object> propertyMap = dataExportProperties.stream()
                .filter(dataExportProperty -> dataFormatterFactory.getPropertySpec(dataExportProperty.getName()).isPresent())
                .filter(property -> (property.useDefault() ? getDefaultValue(dataFormatterFactory, property) : property.getValue()) != null)
                .collect(Collectors.toMap(DataExportProperty::getName, property -> property.useDefault() ? getDefaultValue(dataFormatterFactory, property) : property.getValue()));
        return dataFormatterFactory.createDataFormatter(propertyMap);
    }

    private DataSelector getDataSelector(IExportTask task, Logger logger, IDataExportOccurrence occurrence) {
        Instant at = occurrence.getRetryTime().orElse(occurrence.getTriggerTime());
        List<DataExportProperty> dataExportProperties = task.getDataExportProperties(at);
        DataSelectorFactory dataSelectorFactory = task.getDataSelectorFactory();
        Map<String, Object> propertyMap = dataExportProperties.stream()
                .filter(dataExportProperty -> dataSelectorFactory.getPropertySpec(dataExportProperty.getName()).isPresent())
                .filter(property -> (property.useDefault() ? getDefaultValue(dataSelectorFactory, property) : property.getValue()) != null)
                .collect(Collectors.toMap(DataExportProperty::getName, property -> property.useDefault() ? getDefaultValue(dataSelectorFactory, property) : property.getValue()));
        return dataSelectorFactory.createDataSelector(propertyMap, logger);
    }

    private Object getDefaultValue(HasDynamicProperties hasDynamicProperties, DataExportProperty property) {
        return hasDynamicProperties.getPropertySpecs().stream().filter(dep -> dep.getName().equals(property.getName()))
                .findFirst().orElseThrow(IllegalArgumentException::new).getPossibleValues().getDefault();
    }

    private FormattedData doProcessFromDefaultReadingSelector(DataExportOccurrence occurrence, Stream<ExportData> exportData, ItemExporter itemExporter) {
        List<FormattedExportData> formattedData = exportData
                .map(MeterReadingData.class::cast)
                .flatMap(meterReadingData -> doProcess(occurrence, meterReadingData, itemExporter).stream())
                .sorted(Comparator.comparing(data -> data.getStructureMarker().getStructurePath().get(0)))
                .collect(Collectors.toList());
        return SimpleFormattedData.of(formattedData);
    }

    private List<FormattedExportData> doProcess(DataExportOccurrence occurrence, MeterReadingData meterReadingData, ItemExporter itemExporter) {
        try {
            return itemExporter.exportItem(occurrence, meterReadingData);
        } catch (DataExportException e) {
            // not fatal, we continue.
            return Collections.emptyList();
        }
    }

    private static final class ExceptionsToFatallyFailed implements Runnable {
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

    private final class LoggingExceptions implements Runnable {

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

    private class LazyItemExporter implements ItemExporter {

        private final DataFormatter dataFormatter;
        private final Logger logger;
        private ItemExporter lazy;

        LazyItemExporter(DataFormatter dataFormatter, Logger logger) {
            this.dataFormatter = dataFormatter;
            this.logger = logger;
        }

        @Override
        public List<FormattedExportData> exportItem(DataExportOccurrence occurrence, MeterReadingData item) {
            if (lazy == null) {
                lazy = getItemExporter(dataFormatter, logger);
            }
            return lazy.exportItem(occurrence, item);
        }

        @Override
        public void done() {
            if (lazy != null) {
                lazy.done();
            }
        }
    }

    private final class TagReplacerFactoryForOccurrence implements TagReplacerFactory {
        private final int sequenceNumber;

        TagReplacerFactoryForOccurrence(IDataExportOccurrence occurrence) {
            Instant startOfDay = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).with(ChronoField.MILLI_OF_DAY, 0L).toInstant();
            this.sequenceNumber = occurrence.nthSince(startOfDay);
        }

        @Override
        public TagReplacer forMarker(StructureMarker structureMarker) {
            return TagReplacerImpl.asTagReplacer(clock, structureMarker, sequenceNumber);
        }
    }

}
