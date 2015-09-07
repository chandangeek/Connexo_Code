package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ReadingTypeDataSelector;
import com.elster.jupiter.export.StructureMarker;
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
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.export.impl.IntervalReadingImpl.intervalReading;
import static com.elster.jupiter.export.impl.ReadingImpl.reading;
import static com.elster.jupiter.util.streams.ExtraCollectors.toImmutableRangeSet;

class DefaultItemDataSelector implements ItemDataSelector {

    private final Clock clock;
    private final ValidationService validationService;

    public DefaultItemDataSelector(Clock clock, ValidationService validationService) {
        this.clock = clock;
        this.validationService = validationService;
    }

    @Override
    public Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, IReadingTypeDataExportItem item) {
        Range<Instant> exportInterval = determineExportInterval(occurrence, item);
        List<? extends BaseReadingRecord> readings = new ArrayList<>(item.getReadingContainer().getReadings(exportInterval, item.getReadingType()));

        DataExportStrategy strategy = item.getSelector().getStrategy();

        handleValidatedDataOption(item, strategy, readings, exportInterval);

        if (strategy.isExportCompleteData() && !isComplete(item, exportInterval, readings)) {
            return Optional.empty();
        }

        if (!readings.isEmpty()) {
            MeterReadingImpl meterReading = asMeterReading(item, readings);
            return Optional.of(new MeterReadingData(item, meterReading, structureMarker(item, readings.get(0).getTimeStamp())));
        }
        return Optional.empty();
    }

    private void handleValidatedDataOption(IReadingTypeDataExportItem item, DataExportStrategy strategy, List<? extends BaseReadingRecord> readings, Range<Instant> interval) {
        switch (strategy.getValidatedDataOption()) {
            case EXCLUDE_INTERVAL:
                handleExcludeInterval(item, readings, interval);
                return;
            case EXCLUDE_ITEM:
                handleExcludeItem(item, readings, interval);
            default:
        }
    }

    private void handleExcludeItem(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings, Range<Instant> interval) {
        if (hasUnvalidatedReadings(item, readings) || hasSuspects(item, interval)) {
            readings.clear();
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
            return;
        }
        lastChecked.ifPresent(date -> readings.removeIf(baseReadingRecord -> baseReadingRecord.getTimeStamp().isAfter(date)));

        Set<Instant> invalids = getSuspects(item, interval)
                .collect(Collectors.toSet());
        readings.removeIf(baseReadingRecord -> invalids.contains(baseReadingRecord.getTimeStamp()));
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
        base.remove(occurrence.getExportedDataInterval());
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
