package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeSystem;
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
     * @return the {@link ReadingQualityFetcher} that will help to define the desired criteria
     * for search of {@link ReadingQualityRecord ReadingQualityRecords}
     */
    ReadingQualityFetcher findReadingQualities();

    boolean isRegular();

    List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount);

    List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount);

    boolean hasMacroPeriod();

    boolean hasData();

    /**
     * Sets a given list of {@link BaseReading BaseReadings} as addition/editing result.
     * @param system {@link QualityCodeSystem} that handles editing.
     * @param readings A list of {@link BaseReading BaseReadings} to put to channel.
     */
    void editReadings(QualityCodeSystem system, List<? extends BaseReading> readings);

    /**
     * Sets a given list of {@link BaseReading BaseReadings} as confirmation result.
     * @param system {@link QualityCodeSystem} that handles confirmation.
     * @param readings A list of {@link BaseReading BaseReadings} to put to channel.
     */
    void confirmReadings(QualityCodeSystem system, List<? extends BaseReading> readings);

    /**
     * Removes a given list of {@link BaseReadingRecord BaseReadingRecords}.
     * @param system {@link QualityCodeSystem} that handles removal.
     * @param readings A list of {@link BaseReadingRecord BaseReadingRecords} to remove from channel.
     */
    void removeReadings(QualityCodeSystem system, List<? extends BaseReadingRecord> readings);

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
