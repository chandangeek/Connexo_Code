package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeIndex;
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
     * @param type
     * @param timestamp
     * @return
     * @deprecated unused in production and likely unneeded due to {@link #findReadingQualities(Instant)}
     */
    @Deprecated
    Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Instant timestamp);

    /**
     * Looks for reading qualities of any of given {@param qualityCodeSystems} and of a given {@param index} present in a given {@param interval}
     *
     * @param qualityCodeSystems only systems to take into account when looking for qualities; <code>null</code> or empty set mean all systems
     * @param index quality index to find; <code>null</code> means any index
     * @param interval interval to check for qualities; <code>null</code> acts the same way as {@link Range#all()}
     * @param checkIfActual whether or not to look for actual qualities only
     * @return the list of found qualities
     */
    List<ReadingQualityRecord> findReadingQualities(Set<QualityCodeSystem> qualityCodeSystems, QualityCodeIndex index,
                                                    Range<Instant> interval,
                                                    boolean checkIfActual);

    List<ReadingQualityRecord> findReadingQualities(Instant timestamp);

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
