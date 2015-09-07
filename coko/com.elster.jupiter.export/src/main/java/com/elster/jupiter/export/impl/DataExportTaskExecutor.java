package com.elster.jupiter.export.impl;

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
import com.elster.jupiter.export.SimpleFormattedData;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasDynamicProperties;
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

    public DataExportTaskExecutor(IDataExportService dataExportService, TransactionService transactionService, LocalFileWriter localFileWriter, Thesaurus thesaurus, Clock clock) {
        this.dataExportService = dataExportService;
        this.transactionService = transactionService;
        this.localFileWriter = localFileWriter;
        this.thesaurus = thesaurus;
        this.clock = clock;
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
        Exception thrown = null;
        Logger occurrenceLogger = getLogger(occurrence);
        try {
            doExecute(dataExportOccurrence, occurrenceLogger);
            success = true;
        } catch (Exception ex) {
            thrown = ex;
            errorMessage = ex.getMessage();
            if (is(errorMessage).emptyOrOnlyWhiteSpace()) {
                errorMessage = ex.toString();
            }
            throw ex;
        } finally {
            try (TransactionContext transactionContext = transactionService.getContext()) {
                if (thrown != null) {
                    occurrenceLogger.log(Level.SEVERE, errorMessage, thrown);
                }
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
        IExportTask task = occurrence.getTask();

        Stream<ExportData> data = getDataSelector(task, logger).selectData(occurrence);

        DataFormatter dataFormatter = getDataFormatter(task);

        catchingUnexpected(loggingExceptions(logger, () -> dataFormatter.startExport(occurrence, logger))).run();

        ItemExporter itemExporter = new LazyItemExporter(dataFormatter, logger);

        catchingUnexpected(() -> {
            FormattedData formattedData;
            if (task.hasDefaultSelector()) {
                formattedData = doProcessFromDefaultSelector(dataFormatter, occurrence, data, itemExporter);
            } else {
                formattedData = doProcess(dataFormatter, occurrence, data, itemExporter);
            }
            Map<StructureMarker, Path> files = localFileWriter.writeToTempFiles(formattedData.getData());
            task.getCompositeDestination().send(files, new TagReplacerFactoryForOccurrence(occurrence));
        }).run();

        itemExporter.done();

        catchingUnexpected(loggingExceptions(logger, dataFormatter::endExport)).run();

    }

    private LoggingItemExporter getItemExporter(DataFormatter dataFormatter, Logger logger) {
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

    private DataFormatter getDataFormatter(IExportTask task) {
        List<DataExportProperty> dataExportProperties = task.getDataExportProperties();
        DataFormatterFactory dataFormatterFactory = getDataFormatterFactory(task.getDataFormatter());
        Map<String, Object> propertyMap = dataExportProperties.stream()
                .filter(dataExportProperty -> dataFormatterFactory.getPropertySpec(dataExportProperty.getName()) != null)
                .filter(property -> (property.useDefault() ? getDefaultValue(dataFormatterFactory, property) : property.getValue()) != null)
                .collect(Collectors.toMap(DataExportProperty::getName, property -> property.useDefault() ? getDefaultValue(dataFormatterFactory, property) : property.getValue()));
        return dataFormatterFactory.createDataFormatter(propertyMap);
    }

    private DataSelector getDataSelector(IExportTask task, Logger logger) {
        List<DataExportProperty> dataExportProperties = task.getDataExportProperties();
        DataSelectorFactory dataSelectorFactory = getDataSelectorFactory(task.getDataSelector());
        Map<String, Object> propertyMap = dataExportProperties.stream()
                .filter(dataExportProperty -> dataSelectorFactory.getPropertySpec(dataExportProperty.getName()) != null)
                .filter(property -> (property.useDefault() ? getDefaultValue(dataSelectorFactory, property) : property.getValue()) != null)
                .collect(Collectors.toMap(DataExportProperty::getName, property -> property.useDefault() ? getDefaultValue(dataSelectorFactory, property) : property.getValue()));
        return dataSelectorFactory.createDataSelector(propertyMap, logger);
    }

    private Object getDefaultValue(HasDynamicProperties hasDynamicProperties, DataExportProperty property) {
        return hasDynamicProperties.getPropertySpecs().stream().filter(dep -> dep.getName().equals(property.getName()))
                .findFirst().orElseThrow(IllegalArgumentException::new).getPossibleValues().getDefault();
    }

    private DataFormatterFactory getDataFormatterFactory(String dataFormatter) {
        return dataExportService.getDataFormatterFactory(dataFormatter).orElseThrow(() -> new NoSuchDataFormatter(thesaurus, dataFormatter));
    }

    private DataSelectorFactory getDataSelectorFactory(String dataSelector) {
        return dataExportService.getDataSelectorFactory(dataSelector).orElseThrow(() -> new NoSuchDataSelector(thesaurus, dataSelector));
    }

    //TODO get the data to the destinations
    private FormattedData doProcess(DataFormatter dataFormatter, DataExportOccurrence occurrence, Stream<ExportData> exportData, ItemExporter itemExporter) {
        return dataFormatter.processData(exportData);
    }

    private FormattedData doProcessFromDefaultSelector(DataFormatter dataFormatter, DataExportOccurrence occurrence, Stream<ExportData> exportDatas, ItemExporter itemExporter) {
        List<FormattedExportData> formattedDatas = exportDatas
                .map(MeterReadingData.class::cast)
                .flatMap(meterReadingData -> doProcess(occurrence, meterReadingData, itemExporter).stream())
                .collect(Collectors.toList());
        return SimpleFormattedData.of(formattedDatas);
    }

    private List<FormattedExportData> doProcess(DataExportOccurrence occurrence, MeterReadingData meterReadingData, ItemExporter itemExporter) {
        try {
            return itemExporter.exportItem(occurrence, meterReadingData);
        } catch (DataExportException e) {
            // not fatal, we continue.
            return Collections.emptyList();
        }
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

    private class LazyItemExporter implements ItemExporter {

        private final DataFormatter dataFormatter;
        private final Logger logger;
        private ItemExporter lazy;

        public LazyItemExporter(DataFormatter dataFormatter, Logger logger) {
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

    private class TagReplacerFactoryForOccurrence implements TagReplacerFactory {
        private final int sequenceNumber;

        public TagReplacerFactoryForOccurrence(IDataExportOccurrence occurrence) {
            Instant startOfDay = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).with(ChronoField.MILLI_OF_DAY, 0L).toInstant();
            this.sequenceNumber = occurrence.nthSince(startOfDay);
        }

        @Override
        public TagReplacer forMarker(StructureMarker structureMarker) {
            return TagReplacerImpl.asTagReplacer(clock, structureMarker, sequenceNumber);
        }
    }
}
