/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.impl.ChannelImpl;
import com.elster.jupiter.metering.readings.BaseReading;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface CimChannel {

    Channel getChannel();

    default ChannelsContainer getChannelContainer() {
        return getChannel().getChannelsContainer();
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

    /**
     * Initializes a new search of {@link ReadingQualityRecord ReadingQualityRecords}
     *
     * @return the {@link ReadingQualityFetcher} that will help to define the desired criteria
     * for search of {@link ReadingQualityRecord ReadingQualityRecords}
     */
    ReadingQualityFetcher findReadingQualities();

    List<IntervalReadingRecord> getIntervalReadings(Range<Instant> interval);

    List<ReadingRecord> getRegisterReadings(Range<Instant> interval);

    List<BaseReadingRecord> getReadings(Range<Instant> interval);

    Optional<BaseReadingRecord> getReading(Instant when);

    List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount);

    List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount);

    /**
     * Sets a given list of {@link BaseReading BaseReadings} as addition/editing result.
     *
     * @param system {@link QualityCodeSystem} that handles editing.
     * @param readings A list of {@link BaseReading BaseReadings} to put to channel.
     */
    void editReadings(QualityCodeSystem system, List<? extends BaseReading> readings);

    /**
     * Sets a given list of {@link BaseReading BaseReadings} as confirmation result.
     *
     * @param system {@link QualityCodeSystem} that handles confirmation.
     * @param readings A list of {@link BaseReading BaseReadings} to put to channel.
     */
    void confirmReadings(QualityCodeSystem system, List<? extends BaseReading> readings);

    /**
     * Sets a given list of {@link BaseReading BaseReadings} as estimation result.
     *
     * @param system {@link QualityCodeSystem} that handles estimation.
     * @param readings A list of {@link BaseReading BaseReadings} to put to channel.
     */
    void estimateReadings(QualityCodeSystem system, List<? extends BaseReading> readings);

    /**
     * TODO: for now this is only a stub; see {@link ChannelImpl#removeReadings(QualityCodeSystem, List)} implementation.
     *
     * @param readings
     */
    @Deprecated
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
