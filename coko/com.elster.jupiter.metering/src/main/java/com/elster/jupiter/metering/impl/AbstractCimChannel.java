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
import com.elster.jupiter.util.Pair;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
        ReadingQualityRecordImpl readingQualityRecord = doCreateReadingQualityRecord(type, baseReading);
        readingQualityRecord.save();
        return readingQualityRecord;
    }

    private ReadingQualityRecordImpl doCreateReadingQualityRecord(ReadingQualityType type, BaseReading baseReading) {
        return ReadingQualityRecordImpl.from(dataModel, type, this, baseReading);
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, Instant timestamp) {
        ReadingQualityRecordImpl readingQualityRecord = ReadingQualityRecordImpl.from(dataModel, type, this, timestamp);
        readingQualityRecord.save();
        return readingQualityRecord;
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, BaseReading baseReading, String comment) {
        ReadingQualityRecordImpl readingQualityRecord = doCreateReadingQualityRecord(type, baseReading, comment);
        readingQualityRecord.save();
        return readingQualityRecord;
    }

    private ReadingQualityRecordImpl doCreateReadingQualityRecord(ReadingQualityType type, BaseReading baseReading, String comment) {
        return ReadingQualityRecordImpl.from(dataModel, type, this, baseReading, comment);
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
            Range<Instant> range = readings.stream().map(BaseReading::getTimeStamp).map(Range::singleton).reduce(Range::span).get();
            Map<Instant, BaseReadingRecord> oldReadings = getChannel().getReadings(range).stream().collect(Collectors.toMap(BaseReading::getTimeStamp, Function.identity()));
            ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.EDIT);
            List<ReadingQualityRecord> toCreate = new ArrayList<>();
            List<ReadingQualityRecord> toUpdate = new ArrayList<>();
            List<ReadingQualityRecord> toDelete = new ArrayList<>();
            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> currentQualityRecords = Optional.ofNullable(readingQualitiesByTimestamp.get(reading.getTimeStamp())).orElseGet(Collections::emptyList);

                Optional<BaseReadingRecord> oldReading = Optional.ofNullable(oldReadings.get(reading.getTimeStamp()));
                ProcessStatus processStatus = processStatusToSet.or(oldReading.map(BaseReadingRecord::getProcessStatus).orElse(ProcessStatus.of()));

                Pair<List<ReadingQualityRecord>, List<ReadingQualityRecord>> qualitiesWhenEditingOrEstimating = cleanObsoleteQualitiesWhenEditingOrEstimating(currentQualityRecords);
                toUpdate.addAll(qualitiesWhenEditingOrEstimating.getFirst());
                toDelete.addAll(qualitiesWhenEditingOrEstimating.getLast());

                currentQualityRecords = Optional.ofNullable(readingQualitiesByTimestamp.get(reading.getTimeStamp())).orElseGet(Collections::emptyList);
                Pair<List<ReadingQualityRecord>, List<ReadingQualityRecord>> processedReadingQualities = processReadingQualities(reading, currentQualityRecords, oldReading, qualityForUpdate, qualityForCreate);
                toCreate.addAll(processedReadingQualities.getFirst());
                toUpdate.addAll(processedReadingQualities.getLast());

                storer.addReading(this, reading, processStatus);
            }
            ReadingQualityRecordImpl.deleteAll(dataModel, toDelete);
            ReadingQualityRecordImpl.updateAll(dataModel, toUpdate);
            ReadingQualityRecordImpl.saveAll(dataModel, toCreate);
            storer.execute(system);
        }
    }

    @Override
    public void confirmReadings(QualityCodeSystem system, List<? extends BaseReading> readings) {
        if (!readings.isEmpty()) {
            ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.CONFIRM);
            Set<QualityCodeSystem> controlledSystems = system == QualityCodeSystem.MDM ? Collections.emptySet() : Collections.singleton(system);
            Map<Instant, List<ReadingQualityRecord>> readingQualitiesByTimestamp = findReadingQualitiesByTimestamp(readings, controlledSystems);
            Range<Instant> range = readings.stream().map(BaseReading::getTimeStamp).map(Range::singleton).reduce(Range::span).get();
            Map<Instant, BaseReadingRecord> oldReadings = getReadings(range).stream().collect(Collectors.toMap(BaseReading::getTimeStamp, Function.identity()));
            List<ReadingQualityRecord> toCreate = new ArrayList<>();
            List<ReadingQualityRecord> toUpdate = new ArrayList<>();
            List<ReadingQualityRecord> toDelete = new ArrayList<>();
            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> currentQualityRecords = Optional.ofNullable(readingQualitiesByTimestamp.get(reading.getTimeStamp())).orElseGet(Collections::emptyList);
                if (currentQualityRecords.stream()
                        .filter(readingQualityRecord -> readingQualityRecord.getType().getSystemCode() == system.ordinal())
                        .anyMatch(ReadingQualityRecord::isSuspect)) {
                    Pair<List<ReadingQualityRecord>, List<ReadingQualityRecord>> qualitiesToUpdate = cleanObsoleteQualities(currentQualityRecords,
                            ReadingQualityType::isSuspect,
                            either(ReadingQualityType::hasValidationCategory).or(ReadingQualityType::isMissing));
                    toUpdate.addAll(qualitiesToUpdate.getFirst());
                    toDelete.addAll(qualitiesToUpdate.getLast());

                    Pair<ReadingQualityRecord, ReadingQualityRecord> confirmedReadingQualityRecord = makeConfirmed(system, reading, currentQualityRecords);
                    if (confirmedReadingQualityRecord.getFirst() != null) {
                        toCreate.add(confirmedReadingQualityRecord.getFirst());
                    } else if (confirmedReadingQualityRecord.getLast() != null) {
                        toUpdate.add(confirmedReadingQualityRecord.getLast());
                    }
                    Optional<BaseReadingRecord> oldReading = Optional.ofNullable(oldReadings.get(reading.getTimeStamp()));
                    ProcessStatus processStatus = ProcessStatus.of(ProcessStatus.Flag.CONFIRMED).or(oldReading.map(BaseReadingRecord::getProcessStatus).orElse(ProcessStatus.of()));
                    storer.addReading(this, reading, processStatus);
                }
            }
            ReadingQualityRecordImpl.deleteAll(dataModel, toDelete);
            ReadingQualityRecordImpl.updateAll(dataModel, toUpdate);
            ReadingQualityRecordImpl.saveAll(dataModel, toCreate);
            storer.execute(system);
        }
    }

    private Pair<ReadingQualityRecord, ReadingQualityRecord> makeConfirmed(QualityCodeSystem system, BaseReading reading, List<ReadingQualityRecord> currentQualityRecords) {
        Optional<ReadingQualityRecord> confirmedQualityRecord = currentQualityRecords.stream()
                .filter(readingQualityRecord -> {
                    ReadingQualityType type = readingQualityRecord.getType();
                    return type.isConfirmed() && type.getSystemCode() == system.ordinal();
                })
                .findFirst();
        if (confirmedQualityRecord.isPresent() && !confirmedQualityRecord.get().isActual()) {
            ((ReadingQualityRecordImpl) confirmedQualityRecord.get()).doMakeActual();
            return Pair.of(null, confirmedQualityRecord.get());
        } else {
            return Pair.of(doCreateReadingQualityRecord(ReadingQualityType.of(system, QualityCodeIndex.ACCEPTED), reading), null);
        }
    }

    @Override
    public void estimateReadings(QualityCodeSystem system, List<? extends BaseReading> readings) {
        if (!readings.isEmpty()) {
            Optional<AbstractCimChannel> derivedCimChannel = derivedCimChannel().map(AbstractCimChannel.class::cast);
            Map<Instant, List<ReadingQualityRecord>> readingQualitiesByTimestamp = findReadingQualitiesByTimestamp(readings, Collections.emptySet());
            Range<Instant> range = readings.stream().map(BaseReading::getTimeStamp).map(Range::singleton).reduce(Range::span).get();
            Map<Instant, BaseReadingRecord> oldReadings = getReadings(range).stream().collect(Collectors.toMap(BaseReading::getTimeStamp, Function.identity()));

            ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.ESTIMATION);
            List<ReadingQualityRecord> toCreate = new ArrayList<>();
            List<ReadingQualityRecord> toUpdate = new ArrayList<>();
            List<ReadingQualityRecord> toDelete = new ArrayList<>();
            for (BaseReading reading : readings) {
                List<ReadingQualityRecord> currentQualityRecords = Optional.ofNullable(readingQualitiesByTimestamp.get(reading.getTimeStamp())).orElse(new ArrayList<>());

                Optional<BaseReadingRecord> oldReading = Optional.ofNullable(oldReadings.get(reading.getTimeStamp()));
                ProcessStatus processStatus = ProcessStatus.of(ProcessStatus.Flag.ESTIMATED).or(oldReading.map(BaseReadingRecord::getProcessStatus).orElse(ProcessStatus.of()));

                List<? extends ReadingQuality> readingQualitiesFromReading = reading.getReadingQualities();

                Pair<List<ReadingQualityRecord>, List<ReadingQualityRecord>> cleanObsoleteQualitiesWhenEditingOrEstimating = cleanObsoleteQualitiesWhenEditingOrEstimating(currentQualityRecords);
                toUpdate.addAll(cleanObsoleteQualitiesWhenEditingOrEstimating.getFirst());
                toDelete.addAll(cleanObsoleteQualitiesWhenEditingOrEstimating.getLast());

                Pair<List<ReadingQualityRecord>, List<ReadingQualityRecord>> processedReadingQualities = processReadingQualities(reading, currentQualityRecords);
                toCreate.addAll(processedReadingQualities.getFirst());
                toUpdate.addAll(processedReadingQualities.getLast());

                storer.addReading(this, reading, processStatus);
                derivedCimChannel.ifPresent(derived -> markEstimated(derived, reading.getTimeStamp(), readingQualitiesFromReading));
            }
            ReadingQualityRecordImpl.deleteAll(dataModel, toDelete);
            ReadingQualityRecordImpl.updateAll(dataModel, toUpdate);
            ReadingQualityRecordImpl.saveAll(dataModel, toCreate);
            storer.execute(system);
        }
    }

    private Pair<List<ReadingQualityRecord>, List<ReadingQualityRecord>> processReadingQualities(BaseReading reading, List<ReadingQualityRecord> currentQualityRecords, Optional<BaseReadingRecord> oldReading, ReadingQualityType qualityForUpdate, ReadingQualityType qualityForCreate) {
        if (!reading.getReadingQualities().isEmpty()) {
            return processReadingQualities(reading, currentQualityRecords);
        } else {
            ReadingQualityRecord readingQuality = doCreateReadingQualityRecord(oldReading.isPresent() ? qualityForUpdate : qualityForCreate, reading);
            return Pair.of(Collections.singletonList(readingQuality), Collections.emptyList());
        }
    }

    private Pair<List<ReadingQualityRecord>, List<ReadingQualityRecord>> processReadingQualities(BaseReading reading, List<ReadingQualityRecord> currentQualityRecords) {
        if (currentQualityRecords.isEmpty()) {
            List<ReadingQualityRecord> collect = reading.getReadingQualities()
                    .stream()
                    .distinct()
                    .map(readingQuality -> doCreateReadingQualityRecord(readingQuality.getType(), reading, readingQuality.getComment()))
                    .collect(Collectors.toList());
            return Pair.of(collect, Collections.emptyList());
        } else {
            List<ReadingQualityRecord> toCreate = new ArrayList<>();
            List<ReadingQualityRecord> toUpdate = new ArrayList<>();
            reading.getReadingQualities().stream()
                    .forEach(readingQuality -> {
                        Optional<ReadingQualityRecord> record = currentQualityRecords.stream()
                                .filter(rec -> rec.getType().equals(readingQuality.getType()))
                                .findFirst();
                        if (record.isPresent()) {
                            ReadingQualityRecord toBeActualRecord = record.get();
                            if (toBeActualRecord.getType().hasProjectedCategory()) {
                                toBeActualRecord.setComment(readingQuality.getComment());
                                ((ReadingQualityRecordImpl) toBeActualRecord).doMakeActual();
                                toUpdate.add(toBeActualRecord);
                            } else {
                                toCreate.add(doCreateReadingQualityRecord(readingQuality.getType(), reading, readingQuality.getComment()));
                            }
                        } else {
                            toCreate.add(doCreateReadingQualityRecord(readingQuality.getType(), reading, readingQuality.getComment()));
                        }
                    });
            return Pair.of(toCreate, toUpdate);
        }
    }

    private Pair<List<ReadingQualityRecord>, List<ReadingQualityRecord>> cleanObsoleteQualitiesWhenEditingOrEstimating(Collection<ReadingQualityRecord> currentQualityRecords) {
        return cleanObsoleteQualities(currentQualityRecords,
                either(ReadingQualityType::isSuspect)
                        .or(qualityType -> qualityType.hasQualityIndex(QualityCodeIndex.OVERFLOWCONDITIONDETECTED))
                        .or(qualityType -> qualityType.hasQualityIndex(QualityCodeIndex.REVERSEROTATION))
                        .or(qualityType -> qualityType.hasEditCategory()
                                && qualityType.getIndexCode() != QualityCodeIndex.ADDED.index()
                                && qualityType.getIndexCode() != QualityCodeIndex.REJECTED.index())
                        .or(ReadingQualityType::hasEstimatedCategory)
                        .or(ReadingQualityType::isConfirmed),
                either(ReadingQualityType::hasValidationCategory)
                        .or(ReadingQualityType::isMissing)
                        .or(qualityType -> qualityType.hasQualityIndex(QualityCodeIndex.ADDED))
                        .or(qualityType -> qualityType.hasQualityIndex(QualityCodeIndex.REJECTED))
                        .or(ReadingQualityType::hasProjectedCategory));
    }

    private void markEstimated(AbstractCimChannel derived, Instant timeStamp, List<? extends ReadingQuality> readingQualities) {
        markDeltaReadingEstimated(derived, timeStamp, readingQualities);
        markNextDeltaReadingEstimated(derived, timeStamp, readingQualities);//added to resolve COMU-3023
    }

    private void markDeltaReadingEstimated(AbstractCimChannel derived, Instant timeStamp, List<? extends ReadingQuality> readingQualities) {
        List<ReadingQualityRecord> readingQualityRecords = derived.findReadingQualities().atTimestamp(timeStamp).collect();
        Pair<List<ReadingQualityRecord>, List<ReadingQualityRecord>> listListPair = cleanObsoleteQualitiesWhenEditingOrEstimating(readingQualityRecords);
        ReadingQualityRecordImpl.updateAll(dataModel, listListPair.getFirst());
        ReadingQualityRecordImpl.deleteAll(dataModel, listListPair.getLast());
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

    private Map<Instant, List<ReadingQualityRecord>> findReadingQualitiesByTimestamp(List<? extends BaseReading> readings, Set<QualityCodeSystem> qualityCodeSystems) {
        Range<Instant> range = readings.stream().map(BaseReading::getTimeStamp).map(Range::singleton).reduce(Range::span).get();
        return findReadingQualities().ofQualitySystems(qualityCodeSystems).inTimeInterval(range).stream()
                .collect(Collectors.groupingBy(ReadingQualityRecord::getReadingTimestamp));
    }

    private Pair<List<ReadingQualityRecord>, List<ReadingQualityRecord>> cleanObsoleteQualities(Collection<ReadingQualityRecord> currentQualityRecords,
                                                                                                Predicate<ReadingQualityType> toRemove,
                                                                                                Predicate<ReadingQualityType> toMakePast) {
        List<ReadingQualityRecord> toUpdate = new ArrayList<>();
        List<ReadingQualityRecord> toDelete = new ArrayList<>();
        currentQualityRecords.forEach(readingQualityRecord -> {
            ReadingQualityType type = readingQualityRecord.getType();
            if (toRemove.test(type)) {
                toDelete.add(readingQualityRecord);
            } else if (readingQualityRecord.isActual() && toMakePast.test(type)) {
                if (readingQualityRecord instanceof ReadingQualityRecordImpl) {
                    ReadingQualityRecordImpl qualityRecord = (ReadingQualityRecordImpl) readingQualityRecord;
                    qualityRecord.doMakePast();
                    toUpdate.add(readingQualityRecord);
                } else {
                    readingQualityRecord.makePast();
                }

            }
        });
        return Pair.of(toUpdate, toDelete);
    }
}
