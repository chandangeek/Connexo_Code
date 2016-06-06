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
     * @param type
     * @param timestamp
     * @return
     * @deprecated unused in production and likely unneeded due to {@link #findReadingQualities(Instant)}
     */
    @Deprecated
    Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Instant timestamp);

    /**
     * @param type
     * @param interval
     * @return
     * @deprecated marked for deletion
     * use {@link #findReadingQualities(Set, QualityCodeIndex, Range, boolean, boolean)} with checkIfActual = false, sort = true
     */
    @Deprecated
    List<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Range<Instant> interval);

    /**
     * @param type
     * @param interval
     * @return
     * @deprecated marked for deletion
     * use {@link #findReadingQualities(Set, QualityCodeIndex, Range, boolean, boolean)} with checkIfActual = true, sort = true
     */
    @Deprecated
    List<ReadingQualityRecord> findActualReadingQuality(ReadingQualityType type, Range<Instant> interval);

    /**
     * Looks for reading qualities of any of given {@param qualityCodeSystems} and of a given {@param index} present in a given {@param interval}
     *
     * @param qualityCodeSystems only systems to take into account when looking for qualities; <code>null</code> or empty set mean all systems
     * @param index quality index to find; <code>null</code> means any index
     * @param interval interval to check for qualities
     * @param checkIfActual whether or not to look for actual qualities only
     * @param sort whether or not to sort the result; the sorting is performed by reading timestamp in chronological manner
     * @return the list of found qualities
     */
    List<ReadingQualityRecord> findReadingQualities(Set<QualityCodeSystem> qualityCodeSystems, QualityCodeIndex index,
                                                    Range<Instant> interval,
                                                    boolean checkIfActual, boolean sort);

    List<ReadingQualityRecord> findReadingQualities(Instant timestamp);

    /**
     * @param interval
     * @return
     * @deprecated marked for deletion
     * use {@link #findReadingQualities(Set, QualityCodeIndex, Range, boolean, boolean)} with systems = null, index = null, checkIfActual = false, sort = true
     */
    @Deprecated
    List<ReadingQualityRecord> findReadingQuality(Range<Instant> interval);


    /**
     * @param interval
     * @return
     * @deprecated marked for deletion
     * use {@link #findReadingQualities(Set, QualityCodeIndex, Range, boolean, boolean)} with systems = null, index = null, checkIfActual = true, sort = true
     */
    @Deprecated
    List<ReadingQualityRecord> findActualReadingQuality(Range<Instant> interval);

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
