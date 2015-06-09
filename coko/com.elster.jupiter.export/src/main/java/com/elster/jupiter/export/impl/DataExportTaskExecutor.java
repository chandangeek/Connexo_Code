package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.FatalDataExportException;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
        IExportTask task = occurrence.getTask();

        Stream<ExportData> data = getDataSelector(task, logger).selectData(occurrence);

        DataProcessor dataFormatter = getDataProcessor(task);

        catchingUnexpected(loggingExceptions(logger, () -> dataFormatter.startExport(occurrence, logger))).run();

        ItemExporter itemExporter = new LazyItemExporter(dataFormatter, logger);

        catchingUnexpected(() -> data.forEach(exportData -> doProcess(dataFormatter, occurrence, exportData, itemExporter))).run();

        itemExporter.done();

        catchingUnexpected(loggingExceptions(logger, dataFormatter::endExport)).run();

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

    private DataProcessor getDataProcessor(IExportTask task) {
        List<DataExportProperty> dataExportProperties = task.getDataExportProperties();
        DataProcessorFactory dataProcessorFactory = getDataProcessorFactory(task.getDataFormatter());
        Map<String, Object> propertyMap = dataExportProperties.stream()
                .filter(dataExportProperty -> dataProcessorFactory.getPropertySpec(dataExportProperty.getName()) != null)
                .filter(property -> (property.useDefault() ? getDefaultValue(dataProcessorFactory, property) : property.getValue()) != null)
                .collect(Collectors.toMap(DataExportProperty::getName, property -> property.useDefault() ? getDefaultValue(dataProcessorFactory, property) : property.getValue()));
        return dataProcessorFactory.createDataFormatter(propertyMap);
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

    private Object getDefaultValue(HasDynamicProperties dataProcessorFactory, DataExportProperty property) {
        return dataProcessorFactory.getPropertySpecs().stream().filter(dep -> dep.getName().equals(property.getName()))
                .findFirst().orElseThrow(IllegalArgumentException::new).getPossibleValues().getDefault();
    }

    private DataProcessorFactory getDataProcessorFactory(String dataFormatter) {
        return dataExportService.getDataProcessorFactory(dataFormatter).orElseThrow(() -> new NoSuchDataProcessor(thesaurus, dataFormatter));
    }

    private DataSelectorFactory getDataSelectorFactory(String dataSelector) {
        return dataExportService.getDataSelectorFactory(dataSelector).orElseThrow(() -> new NoSuchDataSelector(thesaurus, dataSelector));
    }

    private void doProcess(DataProcessor dataProcessor, DataExportOccurrence occurrence, ExportData exportData, ItemExporter itemExporter) {
        if (exportData instanceof MeterReadingData) {
            doProcess(occurrence, (MeterReadingData) exportData, itemExporter);
            return;
        }
        dataProcessor.processData(exportData);
    }

    private void doProcess(DataExportOccurrence occurrence, MeterReadingData meterReadingData, ItemExporter itemExporter) {
        try {
            itemExporter.exportItem(occurrence, meterReadingData);
        } catch (DataExportException e) {
            // not fatal, we continue.
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

        private final DataProcessor dataFormatter;
        private final Logger logger;
        private ItemExporter lazy;

        public LazyItemExporter(DataProcessor dataFormatter, Logger logger) {
            this.dataFormatter = dataFormatter;
            this.logger = logger;
        }

        @Override
        public Range<Instant> exportItem(DataExportOccurrence occurrence, MeterReadingData item) {
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
}
