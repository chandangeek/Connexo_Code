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
import com.elster.jupiter.export.MeterReadingValidationData;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.IntervalReadingRecord;
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
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    CustomMeterReadingItemDataSelector init(Logger logger) {
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

    @Override
    public Optional<MeterReadingData> selectData(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        this.currentExportInterval = adjustedExportPeriod(occurrence, item);

        warnIfExportPeriodCoversFuture(occurrence, currentExportInterval);

        List<BaseReading> readings = getReadings(item, currentExportInterval);

        String itemDescription = item.getDescription();

        if (!readings.isEmpty() && checkInterval(item.getReadingType())) {
            readings = filterReadings(readings);

            List<Instant> instants = new ArrayList<>();
            Instant instant = truncateToDays(currentExportInterval.lowerEndpoint()).plus(1, ChronoUnit.HOURS);

            while (!instant.isAfter(currentExportInterval.upperEndpoint())) {
                instants.add(instant);
                instant = instant.plus(1, ChronoUnit.HOURS);
            }

            Map<Instant, DataValidationStatus> validationStatuses = new HashMap<>();
            List<IntervalBlock> intervalBlocks = new ArrayList<>();
            intervalBlocks.add(buildIntervalBlock(item, readings));
            for (IntervalBlock intervalBlock : intervalBlocks) {
                for (Instant time : instants) {
                    Optional<IntervalReading> readingOpt = intervalBlock.getIntervals().stream()
                            .filter(r -> r.getTimeStamp().equals(time)).findAny();
                    if (readingOpt.isPresent()) {
                        validationStatuses.put(time, new SimpleDataValidationStatusImpl(time, ValidationResult.ACTUAL));
                    } else {
                        readings.add(ZeroIntervalReadingImpl.intervalReading(item.getReadingType(), time));
                        validationStatuses.put(time, new SimpleDataValidationStatusImpl(time, ValidationResult.INVALID));
                    }
                }
            }

            readings.sort(Comparator.comparing(BaseReading::getTimeStamp));
            MeterReadingImpl meterReading = asMeterReading(item, readings);
            exportCount++;
            return Optional.of(new MeterReadingData(item, meterReading, new MeterReadingValidationData(validationStatuses), structureMarker(currentExportInterval)));
        }

        try (TransactionContext context = transactionService.getContext()) {
            MessageSeeds.ITEM_DOES_NOT_HAVE_CREATED_DATA_FOR_EXPORT_WINDOW.log(logger, thesaurus, itemDescription);
            context.commit();
        }

        return Optional.empty();
    }

    private boolean checkInterval(ReadingType readingType) {
        if (readingType.isRegular()) {
            int minutes = readingType.getMeasuringPeriod().getMinutes();
            return minutes > 0 && minutes <= MINUTES_PER_HOUR;
        }
        return false;
    }

    List<BaseReading> filterReadings(List<BaseReading> readings) {
        Map<Instant, BaseReading> map = new HashMap<>();
        for (BaseReading reading : readings) {
            if (reading.getTimeStamp().equals(reading.getTimeStamp().truncatedTo(ChronoUnit.HOURS))) {
                map.put(reading.getTimeStamp(), reading);
            }
        }
        return map.values().stream().sorted(Comparator.comparing(BaseReading::getTimeStamp)).collect(Collectors.toList());
    }

    List<BaseReading> getReadings(ReadingTypeDataExportItem item, Range<Instant> exportInterval) {
        return new ArrayList<>(item.getReadingContainer().getReadings(exportInterval, item.getReadingType()));
    }

    List<BaseReading> getReadingsUpdatedSince(ReadingTypeDataExportItem item, Range<Instant> exportInterval, Instant since) {
        return new ArrayList<>(item.getReadingContainer().getReadingsUpdatedSince(exportInterval, item.getReadingType(), since));
    }

    private Optional<? extends ReadingDataSelectorConfig> getDataSelectorConfig(DataExportOccurrence occurrence) {
        return (occurrence.getTask()).getReadingDataSelectorConfig();
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

    Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        Range<Instant> readingsContainerInterval = item.getReadingContainer() instanceof Effectivity ? ((Effectivity) item.getReadingContainer()).getRange() : Range.all();
        Range<Instant> exportedDataInterval = ((DefaultSelectorOccurrence) occurrence).getExportedDataInterval();
        return item.getLastExportedPeriodEnd()
                .map(lastExport -> getRangeSinceLastExport(exportedDataInterval, lastExport))
                .filter(interval -> Ranges.does(interval).overlap(readingsContainerInterval) || interval.isEmpty())
                .map(interval -> interval.intersection(readingsContainerInterval))
                .map(intersection -> Ranges.copy(intersection).asOpenClosed())
                .orElse(exportedDataInterval);
    }

    private Range<Instant> getRangeSinceLastExport(Range<Instant> exportedDataInterval, Instant lastExport) {
        return (exportedDataInterval.hasUpperBound() && lastExport.isAfter(exportedDataInterval.upperEndpoint())) ?
                Range.openClosed(lastExport, lastExport) :
                copy(exportedDataInterval).withOpenLowerBound(lastExport);
    }

    private Instant truncateToDays(Instant dateTime) {
        return ZonedDateTime.ofInstant(dateTime, ZoneId.systemDefault()).truncatedTo(DAYS).toInstant();
    }

    MeterReadingImpl asMeterReading(ReadingTypeDataExportItem item, List<BaseReading> readings) {
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

        Range<Instant> updateInterval = determineUpdateInterval(occurrence, item);
        List<BaseReading> readings = getReadingsUpdatedSince(item, updateInterval, since);

        Optional<RelativePeriod> updateWindow = item.getSelector().getStrategy().getUpdateWindow();
        if (updateWindow.isPresent()) {
            RelativePeriod window = updateWindow.get();
            RangeSet<Instant> rangeSet = readings.stream()
                    .map(baseReadingRecord -> window.getOpenClosedInterval(
                            ZonedDateTime.ofInstant(baseReadingRecord.getTimeStamp(), item.getReadingContainer().getZoneId())))
                    .collect(toImmutableRangeSet());
            readings = rangeSet.asRanges().stream()
                    .flatMap(range -> {
                        List<BaseReading> found = getReadings(item, range);
                        return found.stream();
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (!readings.isEmpty() && checkInterval(item.getReadingType())) {
            readings = filterReadings(readings);
            MeterReadingImpl meterReading = asMeterReading(item, readings);
            Map<Instant, DataValidationStatus> validationStatuses = new HashMap<>();
            readings.stream().forEach(r -> validationStatuses.put(r.getTimeStamp(), new SimpleDataValidationStatusImpl(r.getTimeStamp(), ValidationResult.ACTUAL)));
            updateCount++;
            return Optional.of(new MeterReadingData(item, meterReading, new MeterReadingValidationData(validationStatuses), structureMarkerForUpdate()));
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

    private Range<Instant> determineUpdateInterval(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        Range<Instant> baseRange;
        TreeRangeSet<Instant> base = TreeRangeSet.create();
        Optional<Instant> adhocTime = occurrence.getTaskOccurrence().getAdhocTime();
        if ((adhocTime.isPresent()) && occurrence.getTask().getRunParameters(adhocTime.get()).isPresent()) {
            DataExportRunParameters runParameters = (occurrence).getTask().getRunParameters(adhocTime.get()).get();
            baseRange = Range.openClosed(runParameters.getUpdatePeriodStart(), runParameters.getUpdatePeriodEnd());
            base.add(baseRange);
            base.remove(currentExportInterval);
        } else {
            baseRange = determineBaseUpdateInterval(occurrence, item);
            base.add(baseRange);
            base.remove(currentExportInterval);
        }
        return base.asRanges().stream().findFirst().orElse(baseRange);
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
        return Optional.of(((MeterReadingSelectorConfig) dataExportOccurrence.getTask().getStandardDataSelectorConfig().get()).getStrategy());
    }
}
