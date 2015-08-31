package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.StorerProcess;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
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
        return ReadingQualityRecordImpl.from(dataModel, type, this, baseReading);
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, Instant timestamp) {
        return ReadingQualityRecordImpl.from(dataModel, type, this, timestamp);
    }

    @Override
    public Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Instant timestamp) {
        return readingQualities()
                .filter(ofType(type))
                .filter(withTimestamp(timestamp))
                .findFirst();
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Range<Instant> interval) {
        return readingQualities()
                .filter(ofType(type))
                .filter(inRange(interval))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadingQualityRecord> findActualReadingQuality(ReadingQualityType type, Range<Instant> interval) {
        return readingQualities()
                .filter(ofType(type))
                .filter(isActual())
                .filter(inRange(interval))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(Instant timestamp) {
        return readingQualities()
                .filter(withTimestamp(timestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(Range<Instant> interval) {
        return readingQualities()
                .filter(inRange(interval))
                .collect(Collectors.toList());
    }

    private QueryStream<ReadingQualityRecord> readingQualities() {
        return dataModel.stream(ReadingQualityRecord.class)
                .filter(ofThisChannel())
                .filter(ofThisReadingType());
    }

    @Override
    public List<ReadingQualityRecord> findActualReadingQuality(Range<Instant> interval) {
        return readingQualities()
                .filter(isActual())
                .filter(inRange(interval))
                .filter(isSuspect())
                .collect(Collectors.toList());
    }

    private Condition isActual() {
        return where("actual").isEqualTo(true);
    }

    private Condition isSuspect() {
        return where("typeCode").isNotNull();
    }

    private Condition inRange(Range<Instant> range) {
        return where("readingTimestamp").in(range);
    }

    private Condition ofThisChannel() {
        return where("channel").isEqualTo(this.getChannel());
    }

    private Condition ofType(ReadingQualityType type) {
        return where("typeCode").isEqualTo(type.getCode());
    }

    private Condition withTimestamp(Instant timestamp) {
        return where("readingTimestamp").isEqualTo(timestamp);
    }

    private Condition ofThisReadingType() {
        return where("readingType").in(getChannel().getReadingTypes());
    }

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
        Optional<TimeSeriesEntry> entryHolder = getChannel().getTimeSeries().getEntry(when);
        if (entryHolder.isPresent()) {
            return Optional.of(createReading(isRegular(), entryHolder.get()).filter(getReadingType()));
        } else {
            return Optional.empty();
        }
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
        if (readings.isEmpty()) {
            return;
        }
        ReadingQualityType qualityForUpdate = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC);
        ReadingQualityType qualityForCreate = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED);
        modifyReadings(readings, qualityForUpdate, qualityForCreate, ProcessStatus.of(ProcessStatus.Flag.EDITED));
    }

    @Override
    public void confirmReadings(List<? extends BaseReading> readings) {
        if (readings.isEmpty()) {
            return;
        }
        ReadingQualityType qualityForUpdate = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED);
        ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.CONFIRM);
        Map<Instant, List<ReadingQualityRecordImpl>> readingQualityByTimestamp = findReadingQualitiesByTimestamp(readings);

        for (BaseReading reading : readings) {
            List<ReadingQualityRecordImpl> currentQualityRecords = Optional.ofNullable(readingQualityByTimestamp.get(reading.getTimeStamp()))
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .filter(r -> r.getReadingType().equals(getReadingType()))
                    .collect(Collectors.toList());

            if (currentQualityRecords.stream().filter(ReadingQualityRecord::isSuspect).findFirst().isPresent()) {
                Optional<BaseReadingRecord> oldReading = getReading(reading.getTimeStamp());
                ProcessStatus processStatus = ProcessStatus.of(ProcessStatus.Flag.CONFIRMED).or(oldReading.map(BaseReadingRecord::getProcesStatus).orElse(ProcessStatus.of()));
                this.createReadingQuality(qualityForUpdate, reading).save();
                makeNoLongerSuspect(currentQualityRecords);
                makeNoLongerEstimated(currentQualityRecords);
                storer.addReading(this, reading, processStatus);
            }
        }
        storer.execute();
    }

    @Override
    public void estimateReadings(List<? extends BaseReading> readings) {
        if (readings.isEmpty()) {
            return;
        }
        Optional<CimChannel> derivedCimChannel = derivedCimChannel();
        Map<Instant, List<ReadingQualityRecordImpl>> readingQualityByTimestamp = findReadingQualitiesByTimestamp(readings);
        ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.ESTIMATION);
        for (BaseReading reading : readings) {
            List<ReadingQualityRecordImpl> currentQualityRecords = Optional.ofNullable(readingQualityByTimestamp.get(reading.getTimeStamp())).orElseGet(Collections::emptyList);
            Optional<BaseReadingRecord> oldReading = getReading(reading.getTimeStamp());
            ProcessStatus processStatus = ProcessStatus.of(ProcessStatus.Flag.ESTIMATED).or(oldReading.map(BaseReadingRecord::getProcesStatus).orElse(ProcessStatus.of()));
            reading.getReadingQualities().stream()
                    .filter(readingQuality -> currentQualityRecords.stream().map(ReadingQualityRecord::getType).noneMatch(type -> type.equals(readingQuality.getType())))
                    .forEach(readingQuality -> createReadingQuality(readingQuality.getType(), reading).save());
            makeNoLongerSuspect(currentQualityRecords);
            storer.addReading(this, reading, processStatus);
            derivedCimChannel.map(AbstractCimChannel.class::cast)
                    .ifPresent(derived -> markEstimated(derived, reading.getTimeStamp(), reading.getReadingQualities()));
        }
        storer.execute();
    }

    private void markEstimated(AbstractCimChannel derived, Instant timeStamp, List<? extends ReadingQuality> readingQualities) {

            List<ReadingQualityRecordImpl> readingQualityRecords = derived.findReadingQuality(timeStamp).stream()
                    .map(ReadingQualityRecordImpl.class::cast)
                    .collect(Collectors.toList());
            derived.makeNoLongerSuspect(readingQualityRecords);
            readingQualities.stream()
                    .filter(readingQuality -> readingQualityRecords.stream().map(ReadingQualityRecord::getType).noneMatch(type -> type.equals(readingQuality.getType())))
                    .forEach(readingQuality -> derived.createReadingQuality(readingQuality.getType(), timeStamp).save());

    }

    public Optional<CimChannel> derivedCimChannel() {
        return getChannel().getDerivedReadingType(getReadingType())
                .flatMap(readingType -> getChannel().getCimChannel(readingType));
    }

    private void modifyReadings(List<? extends BaseReading> readings, ReadingQualityType qualityForUpdate, ReadingQualityType qualityForCreate, ProcessStatus processStatusToSet) {
        Map<Instant, List<ReadingQualityRecordImpl>> readingQualityByTimestamp = findReadingQualitiesByTimestamp(readings);
        ReadingStorer storer = meteringService.createUpdatingStorer(StorerProcess.EDIT);
        for (BaseReading reading : readings) {
            List<ReadingQualityRecordImpl> currentQualityRecords = Optional.ofNullable(readingQualityByTimestamp.get(reading.getTimeStamp())).orElseGet(Collections::emptyList);
            boolean alreadyHasQuality = alreadyHasQuality(currentQualityRecords, ImmutableSet.of(qualityForUpdate, qualityForCreate));
            Optional<BaseReadingRecord> oldReading = getChannel().getReading(reading.getTimeStamp());
            ProcessStatus processStatus = processStatusToSet.or(oldReading.map(BaseReadingRecord::getProcesStatus).orElse(ProcessStatus.of()));
            if (!alreadyHasQuality) {
                this.createReadingQuality(oldReading.isPresent() ? qualityForUpdate : qualityForCreate, reading).save();
            }
            makeNoLongerSuspect(currentQualityRecords);
            makeNoLongerEstimated(currentQualityRecords);
            makeNoLongerConfirmed(currentQualityRecords);
            storer.addReading(this, reading, processStatus);
        }
        storer.execute();
    }

    private Map<Instant, List<ReadingQualityRecordImpl>> findReadingQualitiesByTimestamp(List<? extends BaseReading> readings) {
        Range<Instant> range = readings.stream().map(BaseReading::getTimeStamp).map(Range::singleton).reduce(Range::span).get();
        return findReadingQuality(range).stream()
                .map(ReadingQualityRecordImpl.class::cast)
                .collect(Collectors.groupingBy(ReadingQualityRecordImpl::getReadingTimestamp));
    }

    private boolean alreadyHasQuality(List<ReadingQualityRecordImpl> currentQualityRecords, Collection<ReadingQualityType> qualitiesToCheck) {
        return currentQualityRecords.stream().map(ReadingQualityRecord::getType).anyMatch(qualitiesToCheck::contains);
    }

    private void makeNoLongerSuspect(List<ReadingQualityRecordImpl> currentQualityRecords) {
        currentQualityRecords.stream()
                .filter(ReadingQualityRecordImpl::isSuspect)
                .forEach(ReadingQualityRecordImpl::delete);
        currentQualityRecords.stream()
                .filter(ReadingQualityRecord::isActual)
                .filter(either(ReadingQualityRecord::hasValidationCategory).or(ReadingQualityRecord::isMissing))
                .forEach(ReadingQualityRecordImpl::makePast);
    }

    private void makeNoLongerEstimated(List<ReadingQualityRecordImpl> currentQualityRecords) {
        currentQualityRecords.stream()
                .filter(ReadingQualityRecordImpl::hasEstimatedCategory)
                .forEach(ReadingQualityRecordImpl::makePast);
    }

    private void makeNoLongerConfirmed(List<ReadingQualityRecordImpl> currentQualityRecords) {
        currentQualityRecords.stream()
                .filter(ReadingQualityRecordImpl::isConfirmed)
                .forEach(ReadingQualityRecordImpl::makePast);
    }


}
