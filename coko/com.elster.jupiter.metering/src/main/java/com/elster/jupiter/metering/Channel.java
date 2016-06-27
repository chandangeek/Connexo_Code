package com.elster.jupiter.metering;

import com.elster.jupiter.metering.readings.BaseReading;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface Channel {

    long getId();

    ChannelsContainer getChannelsContainer();

    Optional<TemporalAmount> getIntervalLength();

    List<? extends ReadingType> getReadingTypes();

    List<IntervalReadingRecord> getIntervalReadings(Range<Instant> interval);

    List<BaseReadingRecord> getReadingsUpdatedSince(ReadingType readingType, Range<Instant> interval, Instant since);

    List<ReadingRecord> getRegisterReadings(Range<Instant> interval);

    List<BaseReadingRecord> getReadings(Range<Instant> interval);

    ReadingType getMainReadingType();

    Optional<? extends ReadingType> getBulkQuantityReadingType();

    long getVersion();

    List<ReadingRecord> getRegisterReadings(ReadingType readingType, Range<Instant> interval);

    List<IntervalReadingRecord> getIntervalReadings(ReadingType readingType, Range<Instant> interval);

    List<BaseReadingRecord> getReadings(ReadingType readingType, Range<Instant> interval);

    Optional<BaseReadingRecord> getReading(Instant when);

    ReadingQualityRecord createReadingQuality(ReadingQualityType type, ReadingType readingType, BaseReading baseReading);

    ReadingQualityRecord createReadingQuality(ReadingQualityType type, ReadingType readingType, Instant timestamp);


    /**
     * Initializes a new search of {@link ReadingQualityRecord ReadingQualityRecords}
     *
     * @return the {@link ReadingQualityFilter} that will help to define the desired criteria
     * for search of {@link ReadingQualityRecord ReadingQualityRecords}
     */
    ReadingQualityFilter findReadingQualities();

    boolean isRegular();

    List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount);

    List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount);

    boolean hasMacroPeriod();

    boolean hasData();

    void editReadings(List<? extends BaseReading> readings);

    void confirmReadings(List<? extends BaseReading> readings);

    void removeReadings(List<? extends BaseReadingRecord> readings);

    Instant getFirstDateTime();

    Instant getLastDateTime();

    Instant getNextDateTime(Instant instant);

    Instant getPreviousDateTime(Instant instant);

    List<Instant> toList(Range<Instant> range);

    default boolean hasReadingType(ReadingType readingType) {
        return getReadingTypes().contains(readingType);
    }

    ZoneId getZoneId();

    Optional<CimChannel> getCimChannel(ReadingType readingType);

    interface ReadingsDeletedEvent {
        Channel getChannel();

        Set<Instant> getReadingTimeStamps();

        default Range<Instant> getRange() {
            return Range.encloseAll(getReadingTimeStamps());
        }
    }
}
