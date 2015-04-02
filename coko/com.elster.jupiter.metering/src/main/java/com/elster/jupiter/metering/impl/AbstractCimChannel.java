package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Copyrights EnergyICT
 * Date: 26/03/2015
 * Time: 17:00
 */
public abstract class AbstractCimChannel implements CimChannel {
    protected final DataModel dataModel;

    @Override
    public abstract ChannelImpl getChannel();

    public AbstractCimChannel(DataModel dataModel) {
        this.dataModel = dataModel;
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
                .collect(Collectors.toList());
    }

    private Condition isActual() {
        return where("actual").isEqualTo(true);
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
        return where("readingType").isEqualTo(getReadingType());
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
}
