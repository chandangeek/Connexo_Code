package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.NoSuchDataProcessorException;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Ranges.copy;

/**
 * Copyrights EnergyICT
 * Date: 3/11/2014
 * Time: 9:35
 */
class DataExportTaskExecutor implements TaskExecutor {

    private final IDataExportService dataExportService;
    private final TransactionService transactionService;

    public DataExportTaskExecutor(IDataExportService dataExportService, TransactionService transactionService) {
        this.dataExportService = dataExportService;
        this.transactionService = transactionService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        IDataExportOccurrence dataExportOccurrence = createOccurrence(occurrence);
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        IDataExportOccurrence dataExportOccurrence = findOccurrence(occurrence);
        doExecute(dataExportOccurrence, getLogger(occurrence));
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
        Set<IReadingTypeDataExportItem> activeItems = getActiveItems(task, occurrence);

        task.getExportItems().stream()
                .filter(item -> !activeItems.contains(item))
                .peek(IReadingTypeDataExportItem::deactivate)
                .forEach(IReadingTypeDataExportItem::update);
        activeItems.stream()
                .peek(IReadingTypeDataExportItem::activate)
                .forEach(IReadingTypeDataExportItem::update);

        DataProcessor dataFormatter = getDataProcessor(task);

        dataFormatter.startExport(occurrence, logger);

        activeItems.forEach(item -> doProcess(dataFormatter, occurrence, item));

        dataFormatter.endExport();

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

    private void doProcess(DataProcessor dataFormatter, DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        item.setLastRun(occurrence.getTriggerTime());
        try (TransactionContext context = transactionService.getContext()) {
            dataFormatter.startItem(item);
            Range<Instant> exportInterval = determineExportInterval(occurrence, item);
            List<? extends BaseReadingRecord> readings = item.getReadingContainer().getReadings(exportInterval, item.getReadingType());
            if (!readings.isEmpty()) {
                Optional<Instant> lastExported = dataFormatter.processData(asMeterReading(item, readings));
                lastExported.ifPresent(item::setLastExportedDate);
            }
            dataFormatter.endItem(item);
            context.commit();
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

    private Range<Instant> determineExportInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        return item.getLastExportedDate()
                .map(last -> occurrence.getTask().getStrategy().isExportContinuousData() ? copy(occurrence.getExportedDataInterval()).withOpenLowerBound(last) : occurrence.getExportedDataInterval())
                .orElse(occurrence.getExportedDataInterval());
    }

    private MeterReadingImpl asMeterReading(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        if (item.getReadingType().isRegular()) {
            return getMeterReadingWithIntervalBlock(item, readings);
        }
        return getMeterReadingWithReadings(readings);
    }

    private MeterReadingImpl getMeterReadingWithReadings(List<? extends BaseReadingRecord> readings) {
        return readings.stream()
                .map(Reading.class::cast)
                .collect(
                        MeterReadingImpl::newInstance,
                        (mr, reading) -> mr.addReading(reading),
                        (mr1, mr2) -> mr1.addAllReadings(mr2.getReadings())
                );
    }

    private MeterReadingImpl getMeterReadingWithIntervalBlock(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(buildIntervalBlock(item, readings));
        return meterReading;
    }

    private IntervalBlockImpl buildIntervalBlock(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        return readings.stream()
                .map(IntervalReading.class::cast)
                .collect(
                        () -> IntervalBlockImpl.of(item.getReadingType().getMRID()),
                        (block, reading) -> block.addIntervalReading(reading),
                        (b1, b2) -> b1.addAllIntervalReadings(b2.getIntervals())
                );
    }



}
