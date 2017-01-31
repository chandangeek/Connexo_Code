/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityFetcher;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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
    public ReadingQualityFetcher findReadingQualities() {
        return new ReadingQualityFetcherImpl(dataModel, this);
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
    public void editReadings(QualityCodeSystem system, List<? extends BaseReading> readings) {
        if (!readings.isEmpty()) {
            ReadingQualityType qualityForUpdate = ReadingQualityType.of(system, QualityCodeIndex.EDITGENERIC);
            ReadingQualityType qualityForCreate = ReadingQualityType.of(system, QualityCodeIndex.ADDED);
            ProcessStatus processStatusToSet = ProcessStatus.of(ProcessStatus.Flag.EDITED);
            Map<Instant, List<ReadingQualityRecord>> readingQualitiesByTimestamp = findReadingQualitiesByTimestamp(readings, Collections.emptySet());
            ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.EDIT);
            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> currentQualityRecords = Optional.ofNullable(readingQualitiesByTimestamp.get(reading.getTimeStamp())).orElseGet(Collections::emptyList);
                cleanObsoleteQualitiesWhenEditingOrEstimating(currentQualityRecords);
                Optional<BaseReadingRecord> oldReading = getChannel().getReading(reading.getTimeStamp());
                ProcessStatus processStatus = processStatusToSet.or(oldReading.map(BaseReadingRecord::getProcessStatus).orElse(ProcessStatus.of()));
                createReadingQuality(oldReading.isPresent() ? qualityForUpdate : qualityForCreate, reading);
                storer.addReading(this, reading, processStatus);
            }
            storer.execute(system);
        }
    }

    @Override
    public void confirmReadings(QualityCodeSystem system, List<? extends BaseReading> readings) {
        if (!readings.isEmpty()) {
            ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.CONFIRM);
            Set<QualityCodeSystem> controlledSystems = system == QualityCodeSystem.MDM ? Collections.emptySet() : Collections.singleton(system);
            Map<Instant, List<ReadingQualityRecord>> readingQualitiesByTimestamp = findReadingQualitiesByTimestamp(readings, controlledSystems);
            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> currentQualityRecords = Optional.ofNullable(readingQualitiesByTimestamp.get(reading.getTimeStamp()))
                        .orElseGet(Collections::emptyList);
                if (currentQualityRecords.stream()
                        .filter(readingQualityRecord -> readingQualityRecord.getType().getSystemCode() == system.ordinal())
                        .anyMatch(ReadingQualityRecord::isSuspect)) {
                    cleanObsoleteQualities(currentQualityRecords,
                            ReadingQualityType::isSuspect,
                            either(ReadingQualityType::hasValidationCategory).or(ReadingQualityType::isMissing));
                    makeConfirmed(system, reading, currentQualityRecords);
                    Optional<BaseReadingRecord> oldReading = getReading(reading.getTimeStamp());
                    ProcessStatus processStatus = ProcessStatus.of(ProcessStatus.Flag.CONFIRMED).or(oldReading.map(BaseReadingRecord::getProcessStatus).orElse(ProcessStatus.of()));
                    storer.addReading(this, reading, processStatus);
                }
            }
            storer.execute(system);
        }
    }

    private void makeConfirmed(QualityCodeSystem system, BaseReading reading, List<ReadingQualityRecord> currentQualityRecords) {
        ReadingQualityRecord confirmedQualityRecord = currentQualityRecords.stream()
                .filter(readingQualityRecord -> {
                    ReadingQualityType type = readingQualityRecord.getType();
                    return type.isConfirmed() && type.getSystemCode() == system.ordinal();
                })
                .findFirst()
                .orElse(createReadingQuality(ReadingQualityType.of(system, QualityCodeIndex.ACCEPTED), reading));
        if (!confirmedQualityRecord.isActual()) {
            confirmedQualityRecord.makeActual();
        }
    }

    @Override
    public void estimateReadings(QualityCodeSystem system, List<? extends BaseReading> readings) {
        if (!readings.isEmpty()) {
            Optional<AbstractCimChannel> derivedCimChannel = derivedCimChannel().map(AbstractCimChannel.class::cast);
            Map<Instant, List<ReadingQualityRecord>> readingQualitiesByTimestamp = findReadingQualitiesByTimestamp(readings, Collections.emptySet());
            ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.ESTIMATION);
            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> currentQualityRecords = Optional.ofNullable(readingQualitiesByTimestamp.get(reading.getTimeStamp())).orElseGet(Collections::emptyList);
                Set<ReadingQualityType> currentQualityTypes = currentQualityRecords.stream().map(ReadingQualityRecord::getType).collect(Collectors.toSet());
                Optional<BaseReadingRecord> oldReading = getReading(reading.getTimeStamp());
                ProcessStatus processStatus = ProcessStatus.of(ProcessStatus.Flag.ESTIMATED).or(oldReading.map(BaseReadingRecord::getProcessStatus).orElse(ProcessStatus.of()));
                List<? extends ReadingQuality> readingQualitiesFromReading = reading.getReadingQualities();
                cleanObsoleteQualitiesWhenEditingOrEstimating(currentQualityRecords);
                readingQualitiesFromReading.stream()
                        .map(ReadingQuality::getType)
                        .filter(readingQualityType -> !currentQualityTypes.contains(readingQualityType))
                        .forEach(readingQualityType -> createReadingQuality(readingQualityType, reading));
                storer.addReading(this, reading, processStatus);
                derivedCimChannel
                        .ifPresent(derived -> markEstimated(derived, reading.getTimeStamp(), readingQualitiesFromReading));
            }
            storer.execute(system);
        }
    }

    private static void cleanObsoleteQualitiesWhenEditingOrEstimating(Collection<ReadingQualityRecord> currentQualityRecords) {
        cleanObsoleteQualities(currentQualityRecords,
                either(ReadingQualityType::isSuspect)
                        .or(qualityType -> qualityType.hasEditCategory()
                                && qualityType.getIndexCode() != QualityCodeIndex.ADDED.index())
                        .or(ReadingQualityType::hasEstimatedCategory)
                        .or(ReadingQualityType::isConfirmed),
                either(ReadingQualityType::hasValidationCategory)
                        .or(ReadingQualityType::isMissing)
                        .or(qualityType -> qualityType.qualityIndex().filter(QualityCodeIndex.ADDED::equals).isPresent()));
    }

    private static void markEstimated(AbstractCimChannel derived, Instant timeStamp, List<? extends ReadingQuality> readingQualities) {
        markDeltaReadingEstimated(derived, timeStamp, readingQualities);
        markNextDeltaReadingEstimated(derived, timeStamp, readingQualities);//added to resolve COMU-3023
    }

    private static void markDeltaReadingEstimated(AbstractCimChannel derived, Instant timeStamp, List<? extends ReadingQuality> readingQualities) {
        List<ReadingQualityRecord> readingQualityRecords = derived.findReadingQualities().atTimestamp(timeStamp).collect();
        cleanObsoleteQualitiesWhenEditingOrEstimating(readingQualityRecords);
        Set<ReadingQualityType> presentQualityTypes = readingQualityRecords.stream().map(ReadingQualityRecord::getType).collect(Collectors.toSet());
        readingQualities.stream()
                .map(ReadingQuality::getType)
                .filter(type -> !presentQualityTypes.contains(type))
                .forEach(type -> derived.createReadingQuality(type, timeStamp));
    }

    private static void markNextDeltaReadingEstimated(AbstractCimChannel derived, Instant timeStamp, List<? extends ReadingQuality> readingQualities) {
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

    private Map<Instant, List<ReadingQualityRecord>> findReadingQualitiesByTimestamp(List<? extends BaseReading> readings, Set<QualityCodeSystem> qualityCodeSystems) {
        Range<Instant> range = readings.stream().map(BaseReading::getTimeStamp).map(Range::singleton).reduce(Range::span).get();
        return findReadingQualities().ofQualitySystems(qualityCodeSystems).inTimeInterval(range).stream()
                .collect(Collectors.groupingBy(ReadingQualityRecord::getReadingTimestamp));
    }

    private static void cleanObsoleteQualities(Collection<ReadingQualityRecord> currentQualityRecords,
                                               Predicate<ReadingQualityType> toRemove,
                                               Predicate<ReadingQualityType> toMakePast) {
        currentQualityRecords.forEach(readingQualityRecord -> {
            ReadingQualityType type = readingQualityRecord.getType();
            if (toRemove.test(type)) {
                readingQualityRecord.delete();
            } else if (readingQualityRecord.isActual() && toMakePast.test(type)) {
                readingQualityRecord.makePast();
            }
        });

    }
}
