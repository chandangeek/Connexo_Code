/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportRunParameters;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.MeterReadingValidationData;
import com.elster.jupiter.export.MissingDataOption;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.export.impl.IntervalReadingImpl.intervalReading;
import static com.elster.jupiter.export.impl.ReadingImpl.reading;
import static com.elster.jupiter.util.streams.ExtraCollectors.toImmutableRangeSet;

abstract class AbstractItemDataSelector implements ItemDataSelector {

    private final Clock clock;
    private final Thesaurus thesaurus;
    private final ValidationService validationService;
    private final TransactionService transactionService;
    private final DateTimeFormatter timeFormatter;

    private int exportCount;
    private int updateCount;
    private Logger logger;

    private Range<Instant> currentExportInterval;

    @Inject
    AbstractItemDataSelector(Clock clock,
                             ValidationService validationService,
                             Thesaurus thesaurus,
                             TransactionService transactionService,
                             ThreadPrincipalService threadPrincipalService) {
        this.clock = clock;
        this.validationService = validationService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
        this.timeFormatter = getTimeFormatter(threadPrincipalService.getLocale());
    }

    AbstractItemDataSelector init(Logger logger) {
        this.logger = logger;
        return this;
    }

    public int getExportCount() {
        return exportCount;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    Clock getClock() {
        return clock;
    }

    TransactionService getTransactionService() {
        return transactionService;
    }

    @Override
    public Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        this.currentExportInterval = determineExportInterval(occurrence, item);

        warnIfExportPeriodCoversFuture(occurrence, currentExportInterval);

        List<? extends BaseReadingRecord> readings = getReadings(item, currentExportInterval);

        DataExportStrategy strategy = item.getSelector().getStrategy();

        String itemDescription = item.getDescription();

        handleValidatedDataOption(item, strategy, readings, currentExportInterval, itemDescription);

        if (isExportCompleteData(strategy) && !isComplete(item, currentExportInterval, readings)) {
            if (strategy.getMissingDataOption().equals(MissingDataOption.EXCLUDE_ITEM)) {
                logExportWindow(MessageSeeds.MISSING_WINDOW, currentExportInterval, itemDescription);
            }
            if (strategy.getMissingDataOption().equals(MissingDataOption.EXCLUDE_OBJECT)) {
                logExportWindow(MessageSeeds.USAGE_POINT_MISSING_WINDOW, currentExportInterval, item.getDomainObject().getDescription());
            }
            return Optional.empty();
        }

        if (!isExportCompleteData(strategy)) {
            logMissings(item, currentExportInterval, readings, itemDescription);
        }

        if (!readings.isEmpty()) {
            MeterReadingImpl meterReading = asMeterReading(item, readings);
            MeterReadingValidationData meterReadingValidationData = getValidationData(item, readings, currentExportInterval);
            exportCount++;
            return Optional.of(new MeterReadingData(item, meterReading, meterReadingValidationData, null, structureMarker(currentExportInterval)));
        }

        try (TransactionContext context = transactionService.getContext()) {
            MessageSeeds.ITEM_DOES_NOT_HAVE_CREATED_DATA_FOR_EXPORT_WINDOW.log(logger, thesaurus, itemDescription);
            context.commit();
        }

        return Optional.empty();
    }

    List<BaseReadingRecord> getReadings(ReadingTypeDataExportItem item, Range<Instant> exportInterval) {
        return new ArrayList<>(item.getReadingContainer().getReadings(exportInterval, item.getReadingType()));
    }

    List<BaseReadingRecord> getReadingsUpdatedSince(ReadingTypeDataExportItem item, Range<Instant> exportInterval, Instant since) {
        return new ArrayList<>(item.getReadingContainer().getReadingsUpdatedSince(exportInterval, item.getReadingType(), since));
    }

    private DateTimeFormatter getTimeFormatter(Locale locale) {
        return DefaultDateTimeFormatters.longDate(locale).withLongTime().build().withZone(ZoneId.systemDefault()).withLocale(locale);
    }

    private Optional<? extends ReadingDataSelectorConfig> getDataSelectorConfig(DataExportOccurrence occurrence) {
        return ((IExportTask) occurrence.getTask()).getReadingDataSelectorConfig();
    }

    private void warnIfExportPeriodCoversFuture(DataExportOccurrence occurrence, Range<Instant> exportInterval) {
        if (!exportInterval.hasUpperBound() || clock.instant().isBefore(exportInterval.upperEndpoint())) {
            String relativePeriodName = getDataSelectorConfig(occurrence)
                    .map(DataSelectorConfig::getExportPeriod)
                    .map(RelativePeriod::getName)
                    .get();
            try (TransactionContext context = transactionService.getContext()) {
                MessageSeeds.EXPORT_PERIOD_COVERS_FUTURE.log(logger, thesaurus, relativePeriodName);
                context.commit();
            }
        }
    }

    boolean isComplete(ReadingTypeDataExportItem item, Range<Instant> exportInterval, List<? extends BaseReadingRecord> readings) {
        Set<Instant> instants = new HashSet<>(item.getReadingContainer().toList(item.getReadingType(), exportInterval));
        readings.stream()
                .map(BaseReadingRecord::getTimeStamp)
                .forEach(instants::remove);
        return instants.isEmpty();
    }

    void handleValidatedDataOption(ReadingTypeDataExportItem item, DataExportStrategy strategy,
                                   List<? extends BaseReadingRecord> readings, Range<Instant> interval, String itemDescription) {
        if (!readings.isEmpty()) {
            switch (strategy.getValidatedDataOption()) {
                case EXCLUDE_INTERVAL:
                    handleExcludeInterval(item, readings, interval, itemDescription);
                    break;
                case EXCLUDE_ITEM:
                    handleExcludeItem(item, readings, interval, itemDescription);
                    break;
                case EXCLUDE_OBJECT:
                    handleExcludeObject(item, readings, interval, itemDescription);
                    break;
                default:
            }
        }
    }

    private void handleExcludeItem(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings, Range<Instant> interval, String itemDescription) {
        if (hasUnvalidatedReadings(item, readings) || hasSuspects(item, interval)) {
            logExportWindow(MessageSeeds.SUSPECT_WINDOW, interval, itemDescription);
            readings.clear();
        }
    }

    abstract void handleExcludeObject(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings, Range<Instant> interval, String itemDescription);

    void logExportWindow(MessageSeeds messageSeeds, Range<Instant> interval, String itemDescription) {
        String fromDate = interval.hasLowerBound() ? timeFormatter.format(interval.lowerEndpoint()) : "";
        String toDate = interval.hasUpperBound() ? timeFormatter.format(interval.upperEndpoint()) : "";
        try (TransactionContext context = transactionService.getContext()) {
            messageSeeds.log(logger, thesaurus, fromDate, toDate, itemDescription);
            context.commit();
        }
    }

    private boolean hasSuspects(ReadingTypeDataExportItem item, Range<Instant> interval) {
        return getSuspects(item, interval).findAny().isPresent();
    }

    boolean hasUnvalidatedReadings(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        Optional<Instant> lastChecked = validationService.getEvaluator().getLastChecked(item.getReadingContainer(), item.getReadingType());
        return !lastChecked.isPresent() || readings.stream().anyMatch(baseReadingRecord -> baseReadingRecord.getTimeStamp().isAfter(lastChecked.get()));
    }

    private Stream<Instant> getSuspects(ReadingTypeDataExportItem item, Range<Instant> interval) {
        return item.getReadingContainer()
                .getReadingQualities(getQualityCodeSystems(), QualityCodeIndex.SUSPECT, item.getReadingType(), interval).stream()
                .map(ReadingQualityRecord::getReadingTimestamp);
    }

    private void handleExcludeInterval(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings,
                                       Range<Instant> interval, String itemDescription) {
        Optional<Instant> lastChecked = validationService.getEvaluator().getLastChecked(item.getReadingContainer(), item.getReadingType());

        if (!lastChecked.isPresent()) {
            readings.clear();
            logExportWindow(MessageSeeds.SUSPECT_INTERVAL, interval, itemDescription);
            return;
        }
        Set<Instant> afterLastChecked = readings.stream()
                .map(BaseReadingRecord::getTimeStamp)
                .filter(timestamp -> timestamp.isAfter(lastChecked.get()))
                .collect(Collectors.toCollection(TreeSet::new));
        if (!afterLastChecked.isEmpty()) {
            logInvalids(item, afterLastChecked, itemDescription);
        }

        lastChecked.ifPresent(date -> readings.removeIf(baseReadingRecord -> baseReadingRecord.getTimeStamp().isAfter(date)));

        Set<Instant> invalids = getSuspects(item, interval).collect(Collectors.toCollection(TreeSet::new));
        if (!invalids.isEmpty()) {
            logInvalids(item, invalids, itemDescription);
        }

        readings.removeIf(baseReadingRecord -> invalids.contains(baseReadingRecord.getTimeStamp()));
    }

    private void logInvalids(ReadingTypeDataExportItem item, Set<Instant> instants, String itemDescription) {
        if (!item.getReadingType().isRegular()) {
            return;
        }
        Optional<TimeDuration> requestedReadingInterval = item.getRequestedReadingInterval();
        TemporalAmount intervalLength = requestedReadingInterval.isPresent() ? requestedReadingInterval.get().asTemporalAmount() : item.getReadingType().getIntervalLength().orElse(Duration.ZERO);
        List<ZonedDateTime> zonedDateTimes = instants.stream()
                .map(instant -> ZonedDateTime.ofInstant(instant, item.getReadingContainer().getZoneId()))
                .collect(Collectors.toList());
        logIntervals(zonedDateTimes, intervalLength, MessageSeeds.SUSPECT_INTERVAL, itemDescription);
    }

    private boolean isExportCompleteData(DataExportStrategy strategy) {
        return MissingDataOption.EXCLUDE_INTERVAL != strategy.getMissingDataOption();
    }

    private void logMissings(ReadingTypeDataExportItem item, Range<Instant> exportInterval, List<? extends BaseReadingRecord> readings, String itemDescription) {
        if (!item.getReadingType().isRegular()) {
            return;
        }
        Set<Instant> instants = new TreeSet<>(item.getReadingContainer().toList(item.getReadingType(), exportInterval));
        readings.stream()
                .map(BaseReadingRecord::getTimeStamp)
                .forEach(instants::remove);
        if (instants.isEmpty()) {
            return;
        }
        Optional<TimeDuration> requestedReadingInterval = item.getRequestedReadingInterval();
        TemporalAmount intervalLength = requestedReadingInterval.isPresent() ? requestedReadingInterval.get().asTemporalAmount() : item.getReadingType().getIntervalLength().orElse(Duration.ZERO);
        List<ZonedDateTime> zonedDateTimes = instants.stream()
                .map(instant -> ZonedDateTime.ofInstant(instant, item.getReadingContainer().getZoneId()))
                .collect(Collectors.toList());
        logIntervals(zonedDateTimes, intervalLength, MessageSeeds.MISSING_INTERVAL, itemDescription);
    }

    private void logIntervals(List<ZonedDateTime> zonedDateTimes, TemporalAmount intervalLength, MessageSeeds messageSeed, String itemDescription) {
        int firstIndex = 0;
        ZonedDateTime start = zonedDateTimes.get(firstIndex);
        for (int i = 0; i < zonedDateTimes.size() - 1; i++) {
            start = start.plus(intervalLength);
            if (!start.equals(zonedDateTimes.get(i + 1))) {
                logInterval(zonedDateTimes, firstIndex, i, intervalLength, messageSeed, itemDescription);
                firstIndex = i + 1;
                start = zonedDateTimes.get(i + 1);
            }
        }
        logInterval(zonedDateTimes, firstIndex, zonedDateTimes.size() - 1, intervalLength, messageSeed, itemDescription);
    }

    private void logInterval(List<ZonedDateTime> zonedDateTimes, int startIndex, int endIndex, TemporalAmount intervalLength,
                             MessageSeeds messageSeed, String itemDescription) {
        boolean isSingleInterval = endIndex - startIndex == 1;
        if (isSingleInterval) {
            doLogInterval(zonedDateTimes, startIndex, startIndex, intervalLength, messageSeed, itemDescription);
        } else {
            doLogInterval(zonedDateTimes, startIndex, endIndex, intervalLength, messageSeed, itemDescription);
        }
    }

    private void doLogInterval(List<ZonedDateTime> zonedDateTimes, int startIndex, int endIndex, TemporalAmount intervalLength,
                               MessageSeeds messageSeed, String itemDescription) {
        ZonedDateTime startTimeToLog = zonedDateTimes.get(startIndex).minus(intervalLength);
        ZonedDateTime endTimeToLog = zonedDateTimes.get(endIndex);
        try (TransactionContext context = transactionService.getContext()) {
            messageSeed.log(logger, thesaurus,
                    timeFormatter.format(startTimeToLog),
                    timeFormatter.format(endTimeToLog), itemDescription);
            context.commit();
        }
    }

    private StructureMarker structureMarker(Range<Instant> exportInterval) {
        return DefaultStructureMarker.createRoot(clock, "export").withPeriod(exportInterval);
    }

    private Range<Instant> determineExportInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        return getDataSelectorConfig(occurrence)
                .map(ReadingDataSelectorConfig::getStrategy)
                .map(strategy -> strategy.adjustedExportPeriod(occurrence, item))
                .orElse(Range.all());
    }

    MeterReadingImpl asMeterReading(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        if (item.getReadingType().isRegular()) {
            return getMeterReadingWithIntervalBlock(item, readings);
        }
        return getMeterReadingWithReadings(item, readings);
    }

    private MeterReadingImpl getMeterReadingWithReadings(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        return readings.stream()
                .map(ReadingRecord.class::cast)
                .collect(
                        MeterReadingImpl::newInstance,
                        (mr, reading) -> mr.addReading(forReadingType(reading, item.getReadingType())),
                        (mr1, mr2) -> mr1.addAllReadings(mr2.getReadings())
                );
    }

    private MeterReadingImpl getMeterReadingWithIntervalBlock(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(buildIntervalBlock(item, readings));
        return meterReading;
    }

    private IntervalBlockImpl buildIntervalBlock(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        return readings.stream()
                .map(IntervalReadingRecord.class::cast)
                .collect(
                        () -> IntervalBlockImpl.of(item.getReadingType().getMRID()),
                        (block, reading) -> block.addIntervalReading(forReadingType(reading, item.getReadingType())),
                        (b1, b2) -> b1.addAllIntervalReadings(b2.getIntervals())
                );
    }

    private IntervalReading forReadingType(IntervalReadingRecord readingRecord, ReadingType readingType) {
        return intervalReading(readingRecord, readingType);
    }

    private Reading forReadingType(ReadingRecord readingRecord, ReadingType readingType) {
        return reading(readingRecord, readingType);
    }

    MeterReadingValidationData getValidationData(ReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings, Range<Instant> exportInterval) {
        Map<Instant, DataValidationStatus> statusMap = item.getReadingContainer().getChannelsContainers().stream()
                .filter(channelsContainer -> channelsContainer.overlaps(exportInterval))
                .map(channelsContainer -> channelsContainer.getChannel(item.getReadingType()))
                .flatMap(Functions.asStream())
                .flatMap(channel -> {
                    Range<Instant> intervalOfInterest = channel.getChannelsContainer().getInterval().toOpenClosedRange().intersection(exportInterval);
                    List<BaseReadingRecord> readingsOfInterest = readings.stream()
                            .filter(reading -> intervalOfInterest.contains(reading.getTimeStamp()))
                            .collect(Collectors.toList());
                    if (readingsOfInterest.isEmpty()) {
                        return Stream.empty();
                    } else {
                        return validationService.getEvaluator().getValidationStatus(getQualityCodeSystems(), channel, readingsOfInterest).stream();
                    }
                })
                .collect(Collectors.toMap(DataValidationStatus::getReadingTimestamp, Function.identity()));
        return new MeterReadingValidationData(statusMap);
    }

    @Override
    public Optional<MeterReadingData> selectDataForUpdate(DataExportOccurrence occurrence, ReadingTypeDataExportItem item, Instant since) {
        if (!isExportUpdates(occurrence)) {
            return Optional.empty();
        }

        Optional<Range<Instant>> updateInterval = determineUpdateInterval(occurrence, item);
        if (updateInterval.isPresent()) {
            List<? extends BaseReadingRecord> readings = getReadingsUpdatedSince(item, updateInterval.get(), since);

            String itemDescription = item.getDescription();

            Optional<RelativePeriod> updateWindow = item.getSelector().getStrategy().getUpdateWindow();
            if (updateWindow.isPresent()) {
                RelativePeriod window = updateWindow.get();
                RangeSet<Instant> rangeSet = readings.stream()
                        .map(baseReadingRecord -> window.getOpenClosedInterval(
                                ZonedDateTime.ofInstant(baseReadingRecord.getTimeStamp(), item.getReadingContainer().getZoneId())))
                        .collect(toImmutableRangeSet());
                readings = rangeSet.asRanges().stream()
                        .flatMap(range -> {
                            List<? extends BaseReadingRecord> found = getReadings(item, range);
                            if (getExportStrategy(occurrence).get().getMissingDataOption().equals(MissingDataOption.EXCLUDE_ITEM)) {
                                handleValidatedDataOption(item, item.getSelector().getStrategy(), found, range, itemDescription);
                                if (!isComplete(item, range, found)) {
                                    return Stream.empty();
                                }
                            }
                            return found.stream();
                        })
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            if (!readings.isEmpty()) {
                MeterReadingImpl meterReading = asMeterReading(item, readings);
                MeterReadingValidationData meterReadingValidationData = getValidationData(item, readings, updateInterval.get());
                updateCount++;
                return Optional.of(new MeterReadingData(item, meterReading, meterReadingValidationData, null, structureMarkerForUpdate()));
            }

            try (TransactionContext context = transactionService.getContext()) {
                MessageSeeds.ITEM_DOES_NOT_HAVE_CHANGED_DATA_FOR_UPDATE_WINDOW.log(logger, thesaurus, itemDescription);
                context.commit();
            }
        }
        return Optional.empty();
    }

    private boolean isExportUpdates(DataExportOccurrence occurrence) {
        return getExportStrategy(occurrence).map(DataExportStrategy::isExportUpdate).orElse(false);
    }

    private Optional<Range<Instant>> determineUpdateInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        Range<Instant> baseRange;
        TreeRangeSet<Instant> base = TreeRangeSet.create();
        Optional<Instant> adhocTime = ((IDataExportOccurrence) occurrence).getTaskOccurrence().getAdhocTime();
        if ((adhocTime.isPresent()) && ((IDataExportOccurrence) occurrence).getTask().getRunParameters(adhocTime.get()).isPresent()) {
            DataExportRunParameters runParameters = ((IDataExportOccurrence) occurrence).getTask().getRunParameters(adhocTime.get()).get();
            baseRange = Range.openClosed(runParameters.getUpdatePeriodStart(), runParameters.getUpdatePeriodEnd());
            base.add(baseRange);
            base.remove(currentExportInterval);
        } else {
            baseRange = determineBaseUpdateInterval(occurrence, item);
            base.add(baseRange);
            base.remove(currentExportInterval);
        }
        return base.asRanges().stream().findFirst();
    }

    private Range<Instant> determineBaseUpdateInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        return getExportStrategy(occurrence)
                .filter(DataExportStrategy::isExportUpdate)
                .flatMap(DataExportStrategy::getUpdatePeriod)
                .map(relativePeriod -> relativePeriod.getOpenClosedInterval(
                        ZonedDateTime.ofInstant(occurrence.getTriggerTime(), item.getReadingContainer().getZoneId())))
                .orElse(null);
    }

    private StructureMarker structureMarkerForUpdate() {
        return DefaultStructureMarker.createRoot(getClock(), "update");
    }

    private Optional<DataExportStrategy> getExportStrategy(DataExportOccurrence dataExportOccurrence) {
        IExportTask exportTask = (IExportTask) dataExportOccurrence.getTask();
        return exportTask.getReadingDataSelectorConfig().map(ReadingDataSelectorConfig::getStrategy);
    }

    abstract Set<QualityCodeSystem> getQualityCodeSystems();

}
