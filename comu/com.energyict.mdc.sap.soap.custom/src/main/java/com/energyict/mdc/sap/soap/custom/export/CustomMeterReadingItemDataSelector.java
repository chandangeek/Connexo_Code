/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportRunParameters;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.RangeSets;
import com.elster.jupiter.util.time.RangeBuilder;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.Ranges.copy;
import static com.elster.jupiter.util.streams.ExtraCollectors.toImmutableRangeSet;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.joda.time.DateTimeConstants.MINUTES_PER_HOUR;

class CustomMeterReadingItemDataSelector implements ItemDataSelector {

    private final Clock clock;
    private final Thesaurus thesaurus;
    private final TransactionService transactionService;

    private int exportCount;
    private int updateCount;
    private SAPCustomPropertySets sapCustomPropertySets;
    private Logger logger;

    private Range<Instant> currentExportInterval;

    @Inject
    CustomMeterReadingItemDataSelector(Clock clock,
                                       Thesaurus thesaurus,
                                       TransactionService transactionService) {
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.transactionService = transactionService;
    }

    CustomMeterReadingItemDataSelector init(SAPCustomPropertySets sapCustomPropertySets, Logger logger) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.logger = logger;
        return this;
    }

    int getExportCount() {
        return exportCount;
    }

    int getUpdateCount() {
        return updateCount;
    }

    @Override
    public Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        String itemDescription = item.getDescription();
        // force 1 hour reading export; also done in ZeroIntervalReadingImpl & GapsIntervalReadingImpl
        item.overrideReadingInterval(TimeDuration.hours(1));

        if (sapCustomPropertySets.isRegistered((Meter) item.getDomainObject())) {
            RangeSet<Instant> exportRangeSet = adjustedExportPeriod(occurrence, item);
            currentExportInterval = exportRangeSet.isEmpty() ? null : exportRangeSet.span();

            if (currentExportInterval != null && checkIntervalIsLessThanOrEqualToHour(item.getReadingType())) {
                Instant lowerEndpoint = currentExportInterval.lowerEndpoint();
                Instant upperEndpoint = currentExportInterval.upperEndpoint();
                Instant upperEndpointPlusMilli = upperEndpoint.plus(1, ChronoUnit.MILLIS);
                warnIfExportPeriodCoversFuture(occurrence, currentExportInterval);

                List<Instant> instants = new ArrayList<>();
                Instant instant = truncateToDays(lowerEndpoint).plus(1, ChronoUnit.HOURS);

                while (!instant.isAfter(upperEndpoint)) {
                    if (exportRangeSet.contains(instant)) {
                        instants.add(instant);
                    }
                    instant = instant.plus(1, ChronoUnit.HOURS);
                }

                if (!instants.isEmpty()) {
                    List<BaseReading> readings = filterAndSortReadings(getReadings(item, currentExportInterval))
                            .collect(Collectors.toList());
                    Range<Instant> profileIdsRange = Range.closed(lowerEndpoint, upperEndpointPlusMilli);
                    Map<String, RangeSet<Instant>> profileIds = sapCustomPropertySets.getProfileId(item.getReadingContainer(), item.getReadingType(), profileIdsRange);
                    Map<Instant, String> readingStatuses = new HashMap<>();
                    IntervalBlock intervalBlock = buildIntervalBlock(item, readings);
                    String currentProfileId = getProfileId(profileIds, lowerEndpoint).orElse(null);
                    boolean profileIdChanged = false;
                    IntervalReadingRecord readingToFillGaps;
                    if (!readings.isEmpty()) {
                        readingToFillGaps = (IntervalReadingRecord) readings.get(0);
                    } else {
                        readingToFillGaps = null;
                    }
                    List<Instant> gaps = new ArrayList<>();
                    for (Instant time : instants) {
                        Optional<IntervalReading> readingOptional = intervalBlock.getIntervals().stream()
                                .filter(r -> r.getTimeStamp().equals(time)).findAny();
                        Optional<String> profileId = getProfileId(profileIds, time);
                        if (readingOptional.isPresent()) {
                            readingStatuses.put(time, ReadingStatus.ACTUAL.getValue());
                            if (!profileId.get().equals(currentProfileId)) {
                                fillGaps(readingToFillGaps != null, item, gaps, readings, readingStatuses, readingToFillGaps);
                                currentProfileId = profileId.get();
                                gaps.clear();
                            }
                            readingToFillGaps = ((IntervalReadingImpl) readingOptional.get()).getIntervalReadingRecord();
                            fillGaps(profileIdChanged, item, gaps, readings, readingStatuses, readingToFillGaps);
                            profileIdChanged = false;
                            gaps.clear();
                        } else {
                            if (!profileId.get().equals(currentProfileId)) {
                                fillGaps(readingToFillGaps != null, item, gaps, readings, readingStatuses, readingToFillGaps);
                                readingToFillGaps = null;
                                gaps.clear();
                                profileIdChanged = true;
                                currentProfileId = profileId.get();
                            }
                            gaps.add(time);
                        }
                    }
                    String lastProfileId = getProfileId(profileIds, upperEndpointPlusMilli).orElse(null);
                    fillGaps(readingToFillGaps != null && currentProfileId != null && !currentProfileId.equals(lastProfileId),
                            item, gaps, readings, readingStatuses, readingToFillGaps);

                    readings.sort(Comparator.comparing(BaseReading::getTimeStamp));
                    MeterReadingImpl meterReading = asMeterReading(item, readings);
                    exportCount++;
                    return Optional.of(new MeterReadingData(item, meterReading, null, readingStatuses, structureMarker(currentExportInterval)));
                }
            }
        }

        try (TransactionContext context = transactionService.getContext()) {
            MessageSeeds.ITEM_DOES_NOT_HAVE_CREATED_DATA_FOR_EXPORT_WINDOW.log(logger, thesaurus, itemDescription);
            context.commit();
        }
        item.postponeExportForNewData();
        return Optional.empty();
    }

    private void fillGaps(boolean fillWithReadings, ReadingTypeDataExportItem item, List<Instant> gaps, List<BaseReading> readings, Map<Instant, String> readingStatuses, IntervalReadingRecord readingToFillGaps) {
        if (fillWithReadings) {
            for (Instant gap : gaps) {
                readings.add(GapsIntervalReadingImpl.intervalReading(readingToFillGaps, gap));
                readingStatuses.put(gap, ReadingStatus.ACTUAL.getValue());
            }
        } else {
            for (Instant gap : gaps) {
                readings.add(ZeroIntervalReadingImpl.intervalReading(item.getReadingType(), gap));
                readingStatuses.put(gap, ReadingStatus.INVALID.getValue());
            }
        }
    }

    private Optional<String> getProfileId(Map<String, RangeSet<Instant>> profileIds, Instant time) {
        return profileIds.entrySet().stream()
                .filter(entry -> entry.getValue().contains(time))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private boolean checkIntervalIsLessThanOrEqualToHour(ReadingType readingType) {
        if (readingType.isRegular()) {
            int minutes = readingType.getMeasuringPeriod().getMinutes();
            return minutes > 0 && minutes <= MINUTES_PER_HOUR;
        }
        return false;
    }

    private Stream<BaseReading> filterAndSortReadings(Stream<? extends BaseReading> readings) {
        Map<Instant, BaseReading> map = new TreeMap<>();
        readings.forEach(reading -> {
            if (reading.getTimeStamp().equals(reading.getTimeStamp().truncatedTo(ChronoUnit.HOURS))) {
                map.put(reading.getTimeStamp(), reading);
            }
        });
        return map.values().stream();
    }

    private Stream<? extends BaseReadingRecord> getReadings(ReadingTypeDataExportItem item, Range<Instant> exportInterval) {
        return item.getReadingContainer().getReadings(exportInterval, item.getReadingType()).stream()
                .filter(r -> r.getValue() != null);
    }

    private List<BaseReading> getReadingsUpdatedSince(ReadingTypeDataExportItem item, Range<Instant> exportInterval, Instant since) {
        return item.getReadingContainer().getReadingsUpdatedSince(exportInterval, item.getReadingType(), since).stream()
                .filter(r -> r.getValue() != null)
                .collect(Collectors.toList());
    }

    private Optional<? extends ReadingDataSelectorConfig> getDataSelectorConfig(DataExportOccurrence occurrence) {
        return occurrence.getTask().getReadingDataSelectorConfig();
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

    private StructureMarker structureMarker(Range<Instant> exportInterval) {
        return DefaultStructureMarker.createRoot(clock, "export").withPeriod(exportInterval);
    }

    private RangeSet<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        Range<Instant> readingsContainerInterval = item.getReadingContainer() instanceof Effectivity ? ((Effectivity) item.getReadingContainer()).getInterval().toOpenClosedRange() : Range.all();
        Range<Instant> exportWindowInterval = ((DefaultSelectorOccurrence) occurrence).getExportedDataInterval();
        Optional<Instant> exportStart = item.getLastExportedNewData();
        RangeSet<Instant> profileIdIntervals = exportStart.map(start -> getRangeSinceRequestedDate(exportWindowInterval, start)) // start from the previous period end if exists
                .orElse(Optional.of(exportWindowInterval))
                .map(interval -> getAllProfileIdsRangeSet(item, interval)) // take only the intervals where profile id is set
                .orElseGet(TreeRangeSet::create); // everything is exported
        return profileIdIntervals.subRangeSet(readingsContainerInterval);
    }

    private RangeSet<Instant> getAllProfileIdsRangeSet(ReadingTypeDataExportItem item, Range<Instant> interval) {
        return sapCustomPropertySets.getProfileId(item.getReadingContainer(), item.getReadingType(), interval).values().stream()
                .reduce(RangeSets::union)
                .orElseGet(TreeRangeSet::create);
    }

    private Optional<Range<Instant>> getRangeSinceRequestedDate(Range<Instant> exportedDataInterval, Instant date) {
        return exportedDataInterval.hasUpperBound() && date.isAfter(exportedDataInterval.upperEndpoint()) ?
                Optional.empty() :
                Optional.of(copy(exportedDataInterval).withOpenLowerBound(date));
    }

    private Instant truncateToDays(Instant dateTime) {
        return ZonedDateTime.ofInstant(dateTime, ZoneId.systemDefault()).truncatedTo(DAYS).toInstant();
    }

    private MeterReadingImpl asMeterReading(ReadingTypeDataExportItem item, List<BaseReading> readings) {
        if (item.getReadingType().isRegular()) {
            return getMeterReadingWithIntervalBlock(item, readings);
        }
        return getMeterReadingWithReadings(item, readings);
    }

    private MeterReadingImpl getMeterReadingWithReadings(ReadingTypeDataExportItem item, List<BaseReading> readings) {
        return readings.stream()
                .map(ReadingRecord.class::cast)
                .collect(
                        MeterReadingImpl::newInstance,
                        (mr, reading) -> mr.addReading(forReadingType(reading, item.getReadingType())),
                        (mr1, mr2) -> mr1.addAllReadings(mr2.getReadings())
                );
    }

    private MeterReadingImpl getMeterReadingWithIntervalBlock(ReadingTypeDataExportItem item, List<BaseReading> readings) {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addIntervalBlock(buildIntervalBlock(item, readings));
        return meterReading;
    }

    private IntervalBlockImpl buildIntervalBlock(ReadingTypeDataExportItem item, List<? extends BaseReading> readings) {
        return readings.stream()
                .map(IntervalReadingRecord.class::cast)
                .collect(
                        () -> IntervalBlockImpl.of(item.getReadingType().getMRID()),
                        (block, reading) -> block.addIntervalReading(forReadingType(reading, item.getReadingType())),
                        (b1, b2) -> b1.addAllIntervalReadings(b2.getIntervals())
                );
    }

    private IntervalReading forReadingType(IntervalReadingRecord readingRecord, ReadingType readingType) {
        return IntervalReadingImpl.intervalReading(readingRecord, readingType);
    }

    private Reading forReadingType(ReadingRecord readingRecord, ReadingType readingType) {
        return ReadingImpl.reading(readingRecord, readingType);
    }

    @Override
    public Optional<MeterReadingData> selectDataForUpdate(DataExportOccurrence occurrence, ReadingTypeDataExportItem item, Instant since) {
        if (!isExportUpdates(occurrence)) {
            return Optional.empty();
        }

        if (sapCustomPropertySets.isRegistered((Meter) item.getDomainObject())) {
            Optional<Range<Instant>> updateInterval = determineUpdateInterval(occurrence, item);
            if (updateInterval.isPresent()) {
                List<BaseReading> readings = getReadingsUpdatedSince(item, updateInterval.get(), since);

                Optional<RelativePeriod> updateWindow = item.getSelector().getStrategy().getUpdateWindow();
                if (updateWindow.isPresent()) {
                    RelativePeriod window = updateWindow.get();
                    RangeSet<Instant> rangeSet = readings.stream()
                            .map(baseReadingRecord -> window.getOpenClosedInterval(
                                    ZonedDateTime.ofInstant(baseReadingRecord.getTimeStamp(), item.getReadingContainer().getZoneId())))
                            .collect(toImmutableRangeSet());
                    readings = rangeSet.asRanges().stream()
                            .flatMap(range -> getReadings(item, range))
                            .collect(Collectors.toCollection(ArrayList::new));
                }

                RangeBuilder builder = new RangeBuilder();
                readings.forEach(reading -> builder.add(reading.getTimeStamp()));
                if (builder.hasRange()) {
                    Range<Instant> readingsRange = builder.getRange();
                    RangeSet<Instant> profileIdRangeSet = getAllProfileIdsRangeSet(item, readingsRange);

                    readings = readings.stream()
                            .filter(reading -> profileIdRangeSet.contains(reading.getTimeStamp()))
                            .collect(Collectors.toList());

                    if (!readings.isEmpty() && checkIntervalIsLessThanOrEqualToHour(item.getReadingType())) {
                        readings = filterAndSortReadings(readings.stream())
                                .collect(Collectors.toList());
                        MeterReadingImpl meterReading = asMeterReading(item, readings);
                        Map<Instant, String> readingStatuses = new HashMap<>();
                        readings.forEach(r -> readingStatuses.put(r.getTimeStamp(), ReadingStatus.ACTUAL.getValue()));
                        updateCount++;
                        return Optional.of(new MeterReadingData(item, meterReading, null, readingStatuses, structureMarkerForUpdate()));
                    }
                    item.postponeExportForChangedData();
                }
            }
        } else {
            item.postponeExportForChangedData();
        }

        try (TransactionContext context = transactionService.getContext()) {
            MessageSeeds.ITEM_DOES_NOT_HAVE_CHANGED_DATA_FOR_UPDATE_WINDOW.log(logger, thesaurus, item.getDescription());
            context.commit();
        }
        return Optional.empty();
    }

    private boolean isExportUpdates(DataExportOccurrence occurrence) {
        return getExportStrategy(occurrence).map(DataExportStrategy::isExportUpdate).orElse(false);
    }

    private Optional<Range<Instant>> determineUpdateInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        TreeRangeSet<Instant> base = TreeRangeSet.create();
        Optional<Instant> adhocTime = occurrence.getTaskOccurrence().getAdhocTime();
        Range<Instant> baseRange;
        if ((adhocTime.isPresent()) && occurrence.getTask().getRunParameters(adhocTime.get()).isPresent()) {
            DataExportRunParameters runParameters = (occurrence).getTask().getRunParameters(adhocTime.get()).get();
            baseRange = Range.openClosed(runParameters.getUpdatePeriodStart(), runParameters.getUpdatePeriodEnd());
        } else {
            baseRange = determineBaseUpdateInterval(occurrence, item);
        }
        base.add(baseRange);
        if (currentExportInterval != null) {
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
        return DefaultStructureMarker.createRoot(clock, "update");
    }

    private Optional<DataExportStrategy> getExportStrategy(DataExportOccurrence dataExportOccurrence) {
        return dataExportOccurrence.getTask().getStandardDataSelectorConfig()
                .map(MeterReadingSelectorConfig.class::cast)
                .map(MeterReadingSelectorConfig::getStrategy);
    }
}
