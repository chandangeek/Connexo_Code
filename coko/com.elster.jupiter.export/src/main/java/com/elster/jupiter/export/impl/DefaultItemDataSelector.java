package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.StandardDataSelector;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
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
    private final Thesaurus thesaurus;
    private final DateTimeFormatter timeFormatter = DefaultDateTimeFormatters.longDate().withLongTime().build().withZone(ZoneId.systemDefault());
    private final TransactionService transactionService;
    private int exportCount;

    private Logger logger;

    @Inject
    DefaultItemDataSelector(Clock clock, ValidationService validationService, Thesaurus thesaurus, TransactionService transactionService) {
        this.clock = clock;
        this.validationService = validationService;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
    }

    public int getExportCount() {
        return exportCount;
    }

    private DefaultItemDataSelector init(Logger logger) {
        this.logger = logger;
        return this;
    }

    static DefaultItemDataSelector from(DataModel dataModel, Logger logger) {
        return dataModel.getInstance(DefaultItemDataSelector.class).init(logger);
    }

    @Override
    public Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        Range<Instant> exportInterval = determineExportInterval(occurrence, item);

        if (!exportInterval.hasUpperBound() || clock.instant().isBefore(exportInterval.upperEndpoint())) {
            String relativePeriodName = occurrence.getTask().getReadingTypeDataSelector()
                    .map(IStandardDataSelector.class::cast)
                    .map(standardDataSelector -> standardDataSelector.asReadingTypeDataSelector(logger, thesaurus))
                    .map(ReadingTypeDataSelector.class::cast)
                    .map(ReadingTypeDataSelector::getExportPeriod)
                    .map(RelativePeriod::getName)
                    .get();
            try (TransactionContext context = transactionService.getContext()) {
                MessageSeeds.EXPORT_PERIOD_COVERS_FUTURE.log(logger, thesaurus, relativePeriodName);
                context.commit();
            }

        }

        List<? extends BaseReadingRecord> readings = new ArrayList<>(item.getReadingContainer().getReadings(exportInterval, item.getReadingType()));

        DataExportStrategy strategy = item.getSelector().getStrategy();

        String mrid = item.getReadingContainer().getMeter(occurrence.getTriggerTime()).map(Meter::getMRID).orElse("");
        String itemDescription = mrid + ":" + item.getReadingType().getFullAliasName();

        handleValidatedDataOption(item, strategy, readings, exportInterval, itemDescription);

        if (strategy.isExportCompleteData() && !isComplete(item, exportInterval, readings)) {
            logExportWindow(MessageSeeds.MISSING_WINDOW, exportInterval, itemDescription);
            return Optional.empty();
        }
        if (!strategy.isExportCompleteData()) {
            logMissings(item, exportInterval, readings, itemDescription);
        }

        if (!readings.isEmpty()) {
            MeterReadingImpl meterReading = asMeterReading(item, readings);
            exportCount++;
            return Optional.of(new MeterReadingData(item, meterReading, structureMarker(item, readings.get(0).getTimeStamp(), exportInterval)));
        }

        try (TransactionContext context = transactionService.getContext()) {
            MessageSeeds.ITEM_DOES_NOT_HAVE_DATA_FOR_EXPORT_WINDOW.log(logger, thesaurus, mrid, item.getReadingType().getFullAliasName());
            context.commit();
        }

        return Optional.empty();
    }

    private void handleValidatedDataOption(IReadingTypeDataExportItem item, DataExportStrategy strategy, List<? extends BaseReadingRecord> readings, Range<Instant> interval, String itemDescription) {
        if (validationService.getEvaluator().isValidationEnabled(item.getReadingContainer(), item.getReadingType())) {
            switch (strategy.getValidatedDataOption()) {
                case EXCLUDE_INTERVAL:
                    handleExcludeInterval(item, readings, interval, itemDescription);
                    break;
                case EXCLUDE_ITEM:
                    handleExcludeItem(item, readings, interval, itemDescription);
                default:
            }
        }
    }

    private void handleExcludeItem(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings, Range<Instant> interval, String itemDescription) {
        if (hasUnvalidatedReadings(item, readings) || hasSuspects(item, interval)) {
            logExportWindow(MessageSeeds.SUSPECT_WINDOW, interval, itemDescription);
            readings.clear();
        }
    }

    private void logExportWindow(MessageSeeds messageSeeds, Range<Instant> interval, String itemDescription) {
        String fromDate = interval.hasLowerBound() ? timeFormatter.format(interval.lowerEndpoint()) : "";
        String toDate = interval.hasUpperBound() ? timeFormatter.format(interval.upperEndpoint()) : "";
        try (TransactionContext context = transactionService.getContext()) {
            messageSeeds.log(logger, thesaurus, fromDate, toDate, itemDescription);
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
        return item.getReadingContainer()
                // TODO: update this when export is allowed from MDM; empty set means all systems taken into account
                .getReadingQualities(Collections.emptySet(), QualityCodeIndex.SUSPECT, item.getReadingType(), interval).stream()
                .map(ReadingQualityRecord::getReadingTimestamp);
    }

    private void handleExcludeInterval(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings, Range<Instant> interval, String itemDescription) {
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

    private void logInvalids(IReadingTypeDataExportItem item, Set<Instant> instants, String itemDescription) {
        if (!item.getReadingType().isRegular()) {
            return;
        }
        TemporalAmount intervalLength = item.getReadingType().getIntervalLength().get();
        List<ZonedDateTime> zonedDateTimes = instants.stream()
                .map(instant -> ZonedDateTime.ofInstant(instant, item.getReadingContainer().getZoneId()))
                .collect(Collectors.toList());
        logIntervals(zonedDateTimes, intervalLength, MessageSeeds.SUSPECT_INTERVAL, itemDescription);
    }

    private void logMissings(IReadingTypeDataExportItem item, Range<Instant> exportInterval, List<? extends BaseReadingRecord> readings, String itemDescription) {
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
        logIntervals(zonedDateTimes, intervalLength, MessageSeeds.MISSING_INTERVAL, itemDescription);
    }

    private void logIntervals(List<ZonedDateTime> zonedDateTimes,  TemporalAmount intervalLength, MessageSeeds messageSeed, String itemDescription) {
        int firstIndex = 0;
        ZonedDateTime start = zonedDateTimes.get(firstIndex);
        for (int i = 0; i < zonedDateTimes.size() - 1; i++) {
            start = start.plus(intervalLength);
            if (!start.equals(zonedDateTimes.get(i + 1)))  {
                logInterval(zonedDateTimes, firstIndex, i, intervalLength, messageSeed, itemDescription);
                firstIndex = i + 1;
                start = zonedDateTimes.get(i + 1);
            }
        }
        logInterval(zonedDateTimes, firstIndex, zonedDateTimes.size() - 1, intervalLength, messageSeed, itemDescription);
    }

    private void logInterval(List<ZonedDateTime> zonedDateTimes, int startIndex, int endIndex, TemporalAmount intervalLength, MessageSeeds messageSeed, String itemDescription) {
        boolean isSingleInterval = endIndex - startIndex == 1;
        if (isSingleInterval) {
            doLogInterval(zonedDateTimes, startIndex, startIndex, intervalLength, messageSeed, itemDescription);
        } else {
            doLogInterval(zonedDateTimes, startIndex, endIndex, intervalLength, messageSeed, itemDescription);
        }
    }

    private void doLogInterval(List<ZonedDateTime> zonedDateTimes, int startIndex, int endIndex, TemporalAmount intervalLength, MessageSeeds messageSeed, String itemDescription) {
        ZonedDateTime startTimeToLog = zonedDateTimes.get(startIndex).minus(intervalLength);
        ZonedDateTime endTimeToLog = zonedDateTimes.get(endIndex);
        try (TransactionContext context = transactionService.getContext()) {
            messageSeed.log(logger, thesaurus,
                    timeFormatter.format(startTimeToLog),
                    timeFormatter.format(endTimeToLog), itemDescription);
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

        String mrid = item.getReadingContainer().getMeter(occurrence.getTriggerTime()).map(Meter::getMRID).orElse("");
        String itemDescription = mrid + ":" + item.getReadingType().getFullAliasName();

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
            return Optional.of(new MeterReadingData(item, meterReading, structureMarkerForUpdate(item, readings.get(0).getTimeStamp())));
        }
        return Optional.empty();
    }

    private boolean isExportUpdates(DataExportOccurrence occurrence) {
        return occurrence.getTask().getReadingTypeDataSelector()
                .map(StandardDataSelector::getStrategy)
                .map(DataExportStrategy::isExportUpdate)
                .orElse(false);
    }

    private StructureMarker structureMarker(IReadingTypeDataExportItem item, Instant instant, Range<Instant> exportInterval) {
        return DefaultStructureMarker.createRoot(clock, item.getReadingContainer().getMeter(instant).map(Meter::getMRID).orElse(""))
                .withPeriod(exportInterval)
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
                .map(IStandardDataSelector.class::cast)
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
                .map(StandardDataSelector::getStrategy)
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
