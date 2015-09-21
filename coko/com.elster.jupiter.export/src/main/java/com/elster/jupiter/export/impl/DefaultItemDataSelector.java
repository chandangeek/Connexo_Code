package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.*;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.export.impl.IntervalReadingImpl.intervalReading;
import static com.elster.jupiter.export.impl.ReadingImpl.reading;
import static com.elster.jupiter.util.streams.ExtraCollectors.toImmutableRangeSet;

class DefaultItemDataSelector implements ItemDataSelector {

    private final Clock clock;
    private final ValidationService validationService;
    private final Logger logger;
    private final Thesaurus thesaurus;
    private final DateTimeFormatter timeFormatter = DefaultDateTimeFormatters.mediumDate().withLongTime().build().withZone(ZoneId.systemDefault());
    private final TransactionService transactionService;

    public DefaultItemDataSelector(Clock clock, ValidationService validationService, Logger logger, Thesaurus thesaurus, TransactionService transactionService) {
        this.clock = clock;
        this.validationService = validationService;
        this.logger = logger;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
    }

    @Override
    public Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        Range<Instant> exportInterval = determineExportInterval(occurrence, item);
        List<? extends BaseReadingRecord> readings = new ArrayList<>(item.getReadingContainer().getReadings(exportInterval, item.getReadingType()));

        DataExportStrategy strategy = item.getSelector().getStrategy();

        handleValidatedDataOption(item, strategy, readings, exportInterval);

        if (strategy.isExportCompleteData() && !isComplete(item, exportInterval, readings)) {
            logExportWindow(MessageSeeds.MISSING_WINDOW, exportInterval);
            return Optional.empty();
        }
        if (!strategy.isExportCompleteData()) {
            logMissings(item, exportInterval, readings);
        }


        if (!readings.isEmpty()) {
            MeterReadingImpl meterReading = asMeterReading(item, readings);
            return Optional.of(new MeterReadingData(item, meterReading, structureMarker(item, readings.get(0).getTimeStamp())));
        }
        return Optional.empty();
    }

    private void handleValidatedDataOption(IReadingTypeDataExportItem item, DataExportStrategy strategy, List<? extends BaseReadingRecord> readings, Range<Instant> interval) {
        if (validationService.getEvaluator().isValidationEnabled(item.getReadingContainer(), item.getReadingType())) {
            switch (strategy.getValidatedDataOption()) {
                case EXCLUDE_INTERVAL:
                    handleExcludeInterval(item, readings, interval);
                    return;
                case EXCLUDE_ITEM:
                    handleExcludeItem(item, readings, interval);
                default:
            }
        }
    }

    private void handleExcludeItem(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings, Range<Instant> interval) {
        if (hasUnvalidatedReadings(item, readings) || hasSuspects(item, interval)) {
            logExportWindow(MessageSeeds.SUSPECT_WINDOW, interval);
            readings.clear();
        }
    }

    private void logExportWindow(MessageSeeds messageSeeds, Range<Instant> interval) {
        String fromDate = interval.hasLowerBound() ? timeFormatter.format(interval.lowerEndpoint()) : "";
        String toDate = interval.hasUpperBound() ? timeFormatter.format(interval.upperEndpoint()) : "";
        try (TransactionContext context = transactionService.getContext()) {
            messageSeeds.log(logger, thesaurus, fromDate, toDate);
            context.commit();
        }
    }

    private boolean hasSuspects(IReadingTypeDataExportItem item, Range<Instant> interval) {
        return getSuspects(item, interval).findAny().isPresent();
    }

    private boolean hasUnvalidatedReadings(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        Optional<Instant> lastChecked = validationService.getEvaluator().getLastChecked(item.getReadingContainer(), item.getReadingType());
        return !lastChecked.isPresent() || readings.stream().anyMatch(baseReadingRecord -> baseReadingRecord.getTimeStamp().isAfter(lastChecked.get()));
    }

    private Stream<Instant> getSuspects(IReadingTypeDataExportItem item, Range<Instant> interval) {
        return item.getReadingContainer().getReadingQualities(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), item.getReadingType(), interval).stream()
                .map(ReadingQualityRecord::getReadingTimestamp);
    }

    private void handleExcludeInterval(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings, Range<Instant> interval) {
        Optional<Instant> lastChecked = validationService.getEvaluator().getLastChecked(item.getReadingContainer(), item.getReadingType());

        if (!lastChecked.isPresent()) {
            readings.clear();
            logExportWindow(MessageSeeds.SUSPECT_INTERVAL, interval);
            return;
        }
        Set<Instant> afterLastChecked = readings.stream()
                .map(BaseReadingRecord::getTimeStamp)
                .filter(timestamp -> timestamp.isAfter(lastChecked.get()))
                .collect(Collectors.toCollection(TreeSet::new));
        if (!afterLastChecked.isEmpty()) {
            logInvalids(item, afterLastChecked);
        }

        lastChecked.ifPresent(date -> readings.removeIf(baseReadingRecord -> baseReadingRecord.getTimeStamp().isAfter(date)));

        Set<Instant> invalids = getSuspects(item, interval).collect(Collectors.toCollection(TreeSet::new));
        if (!invalids.isEmpty()) {
            logInvalids(item, invalids);
        }

        readings.removeIf(baseReadingRecord -> invalids.contains(baseReadingRecord.getTimeStamp()));
    }

    private void logInvalids(IReadingTypeDataExportItem item, Set<Instant> instants) {
        if (!item.getReadingType().isRegular()) {
            return;
        }
        TemporalAmount intervalLength = item.getReadingType().getIntervalLength().get();
        List<ZonedDateTime> zonedDateTimes = instants.stream()
                .map(instant -> ZonedDateTime.ofInstant(instant, item.getReadingContainer().getZoneId()))
                .collect(Collectors.toList());
        logIntervals(zonedDateTimes, intervalLength, MessageSeeds.SUSPECT_INTERVAL);
    }

    private void logMissings(IReadingTypeDataExportItem item, Range<Instant> exportInterval, List<? extends BaseReadingRecord> readings) {
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
        TemporalAmount intervalLength = item.getReadingType().getIntervalLength().get();
        List<ZonedDateTime> zonedDateTimes = instants.stream()
                .map(instant -> ZonedDateTime.ofInstant(instant, item.getReadingContainer().getZoneId()))
                .collect(Collectors.toList());
        logIntervals(zonedDateTimes, intervalLength, MessageSeeds.MISSING_INTERVAL);
    }

    private void logIntervals(List<ZonedDateTime> zonedDateTimes,  TemporalAmount intervalLength, MessageSeeds messageSeed) {
        int firstIndex = 0;
        ZonedDateTime start = zonedDateTimes.get(firstIndex);
        for (int i = 0; i < zonedDateTimes.size() - 1; i++) {
            start = start.plus(intervalLength);
            if (!start.equals(zonedDateTimes.get(i + 1)))  {
                logInterval(zonedDateTimes, firstIndex, i, intervalLength, messageSeed);
                firstIndex = i + 1;
                start = zonedDateTimes.get(i + 1);
            }
        }
        logInterval(zonedDateTimes, firstIndex, zonedDateTimes.size() - 1, intervalLength, messageSeed);
    }

    private void logInterval(List<ZonedDateTime> zonedDateTimes, int startIndex, int endIndex, TemporalAmount intervalLength, MessageSeeds messageSeed) {
        boolean isSingleInterval = endIndex - startIndex == 1;
        if (isSingleInterval) {
            doLogInterval(zonedDateTimes, startIndex, startIndex, intervalLength, messageSeed);
        } else {
            doLogInterval(zonedDateTimes, startIndex, endIndex, intervalLength, messageSeed);
        }
    }

    private void doLogInterval(List<ZonedDateTime> zonedDateTimes, int startIndex, int endIndex, TemporalAmount intervalLength, MessageSeeds messageSeed) {
        ZonedDateTime startTimeToLog = zonedDateTimes.get(startIndex).minus(intervalLength);
        ZonedDateTime endTimeToLog = zonedDateTimes.get(endIndex);
        try (TransactionContext context = transactionService.getContext()) {
            messageSeed.log(logger, thesaurus,
                    timeFormatter.format(startTimeToLog),
                    timeFormatter.format(endTimeToLog));
            context.commit();
        }
    }


    private boolean isComplete(IReadingTypeDataExportItem item, Range<Instant> exportInterval, List<? extends BaseReadingRecord> readings) {
        Set<Instant> instants = new HashSet<>(item.getReadingContainer().toList(item.getReadingType(), exportInterval));
        readings.stream()
                .map(BaseReadingRecord::getTimeStamp)
                .forEach(instants::remove);
        return instants.isEmpty();
    }

    @Override
    public Optional<MeterReadingData> selectDataForUpdate(DataExportOccurrence occurrence, IReadingTypeDataExportItem item, Instant since) {
        if (!isExportUpdates(occurrence)) {
            return Optional.empty();
        }
        Range<Instant> updateInterval = determineUpdateInterval(occurrence, item);
        List<? extends BaseReadingRecord> readings = new ArrayList<>(item.getReadingContainer().getReadingsUpdatedSince(updateInterval, item.getReadingType(), since));

        Optional<RelativePeriod> updateWindow = item.getSelector().getStrategy().getUpdateWindow();
        if (updateWindow.isPresent()) {
            RelativePeriod window = updateWindow.get();
            RangeSet<Instant> rangeSet = readings.stream()
                    .map(baseReadingRecord -> window.getOpenClosedInterval(ZonedDateTime.ofInstant(baseReadingRecord.getTimeStamp(), item.getReadingContainer().getZoneId())))
                    .collect(toImmutableRangeSet());
            readings = rangeSet.asRanges().stream()
                    .flatMap(range -> {
                        List<? extends BaseReadingRecord> found = new ArrayList(item.getReadingContainer().getReadings(range, item.getReadingType()));
                        if (occurrence.getTask().getReadingTypeDataSelector().get().getStrategy().isExportCompleteData()) {
                            handleValidatedDataOption(item, item.getSelector().getStrategy(), found, range);
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
            return Optional.of(new MeterReadingData(item, meterReading, structureMarkerForUpdate(item, readings.get(0).getTimeStamp())));
        }
        return Optional.empty();
    }

    private boolean isExportUpdates(DataExportOccurrence occurrence) {
        return occurrence.getTask().getReadingTypeDataSelector()
                .map(ReadingTypeDataSelector::getStrategy)
                .map(DataExportStrategy::isExportUpdate)
                .orElse(false);
    }

    private StructureMarker structureMarker(IReadingTypeDataExportItem item, Instant instant) {
        return DefaultStructureMarker.createRoot(clock, item.getReadingContainer().getMeter(instant).map(Meter::getMRID).orElse(""))
                .child(item.getReadingContainer().getUsagePoint(instant).map(UsagePoint::getMRID).orElse(""))
                .child(item.getReadingType().getMRID() == null ? "" : item.getReadingType().getMRID())
                .child("export");
    }

    private StructureMarker structureMarkerForUpdate(IReadingTypeDataExportItem item, Instant instant) {
        return DefaultStructureMarker.createRoot(clock, item.getReadingContainer().getMeter(instant).map(Meter::getMRID).orElse(""))
                .child(item.getReadingContainer().getUsagePoint(instant).map(UsagePoint::getMRID).orElse(""))
                .child(item.getReadingType().getMRID() == null ? "" : item.getReadingType().getMRID())
                .child("update");
    }

    private Range<Instant> determineExportInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        return occurrence.getTask().getReadingTypeDataSelector()
                .map(IReadingTypeDataSelector.class::cast)
                .map(selector -> selector.adjustedExportPeriod(occurrence, item))
                .orElse(Range.all());
    }

    private Range<Instant> determineUpdateInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        TreeRangeSet<Instant> base = TreeRangeSet.create();
        Range<Instant> baseRange = determineBaseUpdateInterval(occurrence, item);
        base.add(baseRange);
        base.remove(((DefaultSelectorOccurrence) occurrence).getExportedDataInterval());
        return base.asRanges().stream().findFirst().orElse(baseRange);
    }

    private Range<Instant> determineBaseUpdateInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        return occurrence.getTask().getReadingTypeDataSelector()
                .map(ReadingTypeDataSelector::getStrategy)
                .filter(DataExportStrategy::isExportUpdate)
                .flatMap(DataExportStrategy::getUpdatePeriod)
                .map(relativePeriod -> relativePeriod.getOpenClosedInterval(ZonedDateTime.ofInstant(occurrence.getTriggerTime(), item.getReadingContainer().getZoneId())))
                .orElse(null);
    }

    private MeterReadingImpl asMeterReading(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        if (item.getReadingType().isRegular()) {
            return getMeterReadingWithIntervalBlock(item, readings);
        }
        return getMeterReadingWithReadings(item, readings);
    }

    private MeterReadingImpl getMeterReadingWithReadings(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings) {
        return readings.stream()
                .map(ReadingRecord.class::cast)
                .collect(
                        MeterReadingImpl::newInstance,
                        (mr, reading) -> mr.addReading(forReadingType(reading, item.getReadingType())),
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

}
