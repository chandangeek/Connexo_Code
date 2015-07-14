package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.BaseReading;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

public interface CimChannel {
    Channel getChannel();
    default MeterActivation getMeterActivation() {
        return getChannel().getMeterActivation();
    }
    default Optional<TemporalAmount> getIntervalLength() {
        return getChannel().getIntervalLength();
    }
    ReadingType getReadingType();

    default boolean isRegular() {
        return getChannel().isRegular();
    }
    default boolean hasMacroPeriod() {
        return getChannel().hasMacroPeriod();
    }

    ReadingQualityRecord createReadingQuality(ReadingQualityType type, BaseReading baseReading);
    ReadingQualityRecord createReadingQuality(ReadingQualityType type, Instant timestamp);
    Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Instant timestamp);
    List<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Range<Instant> interval);
    List<ReadingQualityRecord> findActualReadingQuality(ReadingQualityType type, Range<Instant> interval);
    List<ReadingQualityRecord> findReadingQuality(Instant timestamp);
    List<ReadingQualityRecord> findReadingQuality(Range<Instant> interval);
    List<ReadingQualityRecord> findActualReadingQuality(Range<Instant> interval);

    List<IntervalReadingRecord> getIntervalReadings(Range<Instant> interval);
    List<ReadingRecord> getRegisterReadings(Range<Instant> interval);
    List<BaseReadingRecord> getReadings(Range<Instant> interval);
    Optional<BaseReadingRecord> getReading(Instant when);
    List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount);
    List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount);

    void editReadings(List<? extends BaseReading> readings);
    void confirmReadings(List<? extends BaseReading> readings);
    void estimateReadings(List<? extends BaseReading> readings);
    void removeReadings(List<? extends BaseReadingRecord> readings);

    default Instant getNextDateTime(Instant instant) {
        return getChannel().getNextDateTime(instant);
    }
    default Instant getPreviousDateTime(Instant instant) {
        return getChannel().getPreviousDateTime(instant);
    }
    default List<Instant> toList(Range<Instant> range) {
        return getChannel().toList(range);
    }

    default ZoneId getZoneId() {
        return getChannel().getZoneId();
    }


}
