package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityFilter;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.either;

public abstract class AbstractCimChannel implements CimChannel {
    private final DataModel dataModel;
    private final MeteringService meteringService;

    @Override
    public abstract ChannelImpl getChannel();

    public AbstractCimChannel(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, BaseReading baseReading) {
        ReadingQualityRecordImpl readingQualityRecord = ReadingQualityRecordImpl.from(dataModel, type, this, baseReading);
        readingQualityRecord.doSave();
        return readingQualityRecord;
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, Instant timestamp) {
        ReadingQualityRecordImpl readingQualityRecord = ReadingQualityRecordImpl.from(dataModel, type, this, timestamp);
        readingQualityRecord.doSave();
        return readingQualityRecord;
    }

    @Override
    public ReadingQualityFilter findReadingQualities() {
        return new ReadingQualityFilterImpl(dataModel, this);
    }

    @Override
    public abstract IReadingType getReadingType();

    @Override
    public List<ReadingRecord> getRegisterReadings(Range<Instant> interval) {
        if (isRegular()) {
            return Collections.emptyList();
        }
        return getChannel().getTimeSeries().getEntries(interval).stream()
                .map(entry -> new ReadingRecordImpl(getChannel(), entry))
                .map(readingRecord -> readingRecord.filter(getReadingType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<IntervalReadingRecord> getIntervalReadings(Range<Instant> interval) {
        if (!isRegular()) {
            return Collections.emptyList();
        }
        return this.getChannel().getTimeSeries().getEntries(interval).stream()
                .map(entry -> new IntervalReadingRecordImpl(getChannel(), entry))
                .map(record -> record.filter(getReadingType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BaseReadingRecord> getReadings(Range<Instant> interval) {
        boolean regular = isRegular();
        return getChannel().getTimeSeries().getEntries(interval).stream()
                .map(entry -> createReading(regular, entry))
                .map(record -> record.filter(getReadingType()))
                .collect(Collectors.toList());
    }

    private BaseReadingRecord createReading(boolean regular, TimeSeriesEntry entry) {
        return regular ? new IntervalReadingRecordImpl(getChannel(), entry) : new ReadingRecordImpl(getChannel(), entry);
    }

    @Override
    public Optional<BaseReadingRecord> getReading(Instant when) {
        return getChannel().getTimeSeries().getEntry(when)
                .map(entryHolder -> createReading(isRegular(), entryHolder).filter(getReadingType()));
    }

    @Override
    public List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount) {
        boolean regular = isRegular();
        return getChannel().getTimeSeries().getEntriesBefore(when, readingCount).stream()
                .map(entry -> createReading(regular, entry))
                .map(record -> record.filter(getReadingType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount) {
        boolean regular = isRegular();
        return getChannel().getTimeSeries().getEntriesOnOrBefore(when, readingCount).stream()
                .map(entry -> createReading(regular, entry))
                .map(record -> record.filter(getReadingType()))
                .collect(Collectors.toList());
    }

    @Override
    public void editReadings(List<? extends BaseReading> readings) {
        if (!readings.isEmpty()) {
            // TODO: refactor with custom QualityCodeSystem(s) in scope of data editing refactoring (CXO-1449)
            ReadingQualityType qualityForUpdate = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC);
            ReadingQualityType qualityForCreate = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED);
            modifyReadings(readings, qualityForUpdate, qualityForCreate, ProcessStatus.of(ProcessStatus.Flag.EDITED));
        }
    }

    @Override
    public void confirmReadings(List<? extends BaseReading> readings) {
        if (!readings.isEmpty()) {
            ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.CONFIRM);
            Map<Instant, List<ReadingQualityRecord>> readingQualityByTimestamp = findReadingQualitiesByTimestamp(readings);

            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> currentQualityRecords = Optional.ofNullable(readingQualityByTimestamp.get(reading.getTimeStamp()))
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .filter(r -> r.getReadingType().equals(getReadingType()))
                        .collect(Collectors.toList());
                if (currentQualityRecords.stream().filter(ReadingQualityRecord::isSuspect).findFirst().isPresent()) {
                    makeNoLongerSuspect(currentQualityRecords);
                    makeNoLongerEstimated(currentQualityRecords);
                    makeConfirmed(reading, currentQualityRecords);
                    Optional<BaseReadingRecord> oldReading = getReading(reading.getTimeStamp());
                    ProcessStatus processStatus = ProcessStatus.of(ProcessStatus.Flag.CONFIRMED).or(oldReading.map(BaseReadingRecord::getProcessStatus).orElse(ProcessStatus.of()));
                    storer.addReading(this, reading, processStatus);
                }
            }
            storer.execute();
        }
    }

    private void makeConfirmed(BaseReading reading, List<ReadingQualityRecord> currentQualityRecords) {
        ReadingQualityRecord confirmedQualityRecord = currentQualityRecords.stream()
                .filter(ReadingQualityRecord::isConfirmed)
                .findFirst()
                .map(ReadingQualityRecord.class::cast)
                // TODO: refactor with custom QualityCodeSystem(s) in scope of data confirming refactoring (CXO-1447)
                .orElseGet(() -> this.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED), reading));
        if (!confirmedQualityRecord.isActual()) {
            confirmedQualityRecord.makeActual();
        }
    }

    @Override
    public void estimateReadings(List<? extends BaseReading> readings) {
        if (!readings.isEmpty()) {
            Optional<AbstractCimChannel> derivedCimChannel = derivedCimChannel().map(AbstractCimChannel.class::cast);
            Map<Instant, List<ReadingQualityRecord>> readingQualitiesByTimestamp = findReadingQualitiesByTimestamp(readings);
            ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.ESTIMATION);
            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> currentQualityRecords = Optional.ofNullable(readingQualitiesByTimestamp.get(reading.getTimeStamp())).orElseGet(Collections::emptyList);
                Set<ReadingQualityType> currentQualityTypes = currentQualityRecords.stream().map(ReadingQualityRecord::getType).collect(Collectors.toSet());
                Optional<BaseReadingRecord> oldReading = getReading(reading.getTimeStamp());
                ProcessStatus processStatus = ProcessStatus.of(ProcessStatus.Flag.ESTIMATED).or(oldReading.map(BaseReadingRecord::getProcessStatus).orElse(ProcessStatus.of()));
                List<? extends ReadingQuality> readingQualitiesFromReading = reading.getReadingQualities();
                readingQualitiesFromReading.stream()
                        .map(ReadingQuality::getType)
                        .filter(readingQualityType -> !currentQualityTypes.contains(readingQualityType))
                        .forEach(readingQualityType -> createReadingQuality(readingQualityType, reading));
                makeNoLongerSuspect(currentQualityRecords);
                storer.addReading(this, reading, processStatus);
                derivedCimChannel
                        .ifPresent(derived -> markEstimated(derived, reading.getTimeStamp(), readingQualitiesFromReading));
            }
            storer.execute();
        }
    }

    private void markEstimated(AbstractCimChannel derived, Instant timeStamp, List<? extends ReadingQuality> readingQualities) {
        markDeltaReadingEstimated(derived, timeStamp, readingQualities);
        markNextDeltaReadingEstimated(derived, timeStamp, readingQualities);//added to resolve COMU-3023
    }

    private void markDeltaReadingEstimated(AbstractCimChannel derived, Instant timeStamp, List<? extends ReadingQuality> readingQualities) {
        List<ReadingQualityRecord> readingQualityRecords = derived.findReadingQualities().atTimestamp(timeStamp).collect();
        makeNoLongerSuspect(readingQualityRecords);
        Set<ReadingQualityType> presentQualityTypes = readingQualityRecords.stream().map(ReadingQualityRecord::getType).collect(Collectors.toSet());
        readingQualities.stream()
                .map(ReadingQuality::getType)
                .filter(type -> !presentQualityTypes.contains(type))
                .forEach(type -> derived.createReadingQuality(type, timeStamp));
    }

    private void markNextDeltaReadingEstimated(AbstractCimChannel derived, Instant timeStamp, List<? extends ReadingQuality> readingQualities) {
        Optional<TemporalAmount> interval = derived.getIntervalLength();
        if (interval.isPresent() && derived.getReading(timeStamp.plus(interval.get())).isPresent()) {
            markDeltaReadingEstimated(derived, timeStamp.plus(interval.get()), readingQualities);
        }
    }

    public Optional<CimChannel> derivedCimChannel() {
        ChannelImpl channel = getChannel();
        return channel.getDerivedReadingType(getReadingType())
                .flatMap(channel::getCimChannel);
    }

    private void modifyReadings(List<? extends BaseReading> readings, ReadingQualityType qualityForUpdate, ReadingQualityType qualityForCreate, ProcessStatus processStatusToSet) {
        Map<Instant, List<ReadingQualityRecord>> readingQualityByTimestamp = findReadingQualitiesByTimestamp(readings);
        ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.EDIT);
        for (BaseReading reading : readings) {
            List<ReadingQualityRecord> currentQualityRecords = Optional.ofNullable(readingQualityByTimestamp.get(reading.getTimeStamp())).orElseGet(Collections::emptyList);
            boolean alreadyHasQuality = alreadyHasQuality(currentQualityRecords, ImmutableSet.of(qualityForUpdate, qualityForCreate));
            Optional<BaseReadingRecord> oldReading = getChannel().getReading(reading.getTimeStamp());
            ProcessStatus processStatus = processStatusToSet.or(oldReading.map(BaseReadingRecord::getProcessStatus).orElse(ProcessStatus.of()));
            if (!alreadyHasQuality) {
                this.createReadingQuality(oldReading.isPresent() ? qualityForUpdate : qualityForCreate, reading);
            }
            makeNoLongerSuspect(currentQualityRecords);
            makeNoLongerEstimated(currentQualityRecords);
            makeNoLongerConfirmed(currentQualityRecords);
            storer.addReading(this, reading, processStatus);
        }
        storer.execute();
    }

    private Map<Instant, List<ReadingQualityRecord>> findReadingQualitiesByTimestamp(List<? extends BaseReading> readings) {
        Range<Instant> range = readings.stream().map(BaseReading::getTimeStamp).map(Range::singleton).reduce(Range::span).get();
        // TODO: refactor with custom QualityCodeSystem(s) in scope of data editing refactoring (CXO-1449)
        return findReadingQualities().inTimeInterval(range).collect().stream()
                .collect(Collectors.groupingBy(ReadingQualityRecord::getReadingTimestamp));
    }

    private static boolean alreadyHasQuality(List<ReadingQualityRecord> currentQualityRecords, Collection<ReadingQualityType> qualitiesToCheck) {
        return currentQualityRecords.stream().map(ReadingQualityRecord::getType).anyMatch(qualitiesToCheck::contains);
    }

    private static void makeNoLongerSuspect(List<ReadingQualityRecord> currentQualityRecords) {
        currentQualityRecords.stream()
                .filter(ReadingQualityRecord::isSuspect)
                .forEach(ReadingQualityRecord::delete);
        currentQualityRecords.stream()
                .filter(ReadingQualityRecord::isActual)
                .filter(either(ReadingQualityRecord::hasValidationCategory).or(ReadingQualityRecord::isMissing))
                .forEach(ReadingQualityRecord::makePast);
    }

    private static void makeNoLongerEstimated(List<ReadingQualityRecord> currentQualityRecords) {
        currentQualityRecords.stream()
                .filter(ReadingQualityRecord::hasEstimatedCategory)
                .forEach(ReadingQualityRecord::makePast);
    }

    private static void makeNoLongerConfirmed(List<ReadingQualityRecord> currentQualityRecords) {
        currentQualityRecords.stream()
                .filter(ReadingQualityRecord::isConfirmed)
                .forEach(ReadingQualityRecord::makePast);
    }
}
