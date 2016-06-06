package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReadingContainer {
    Set<ReadingType> getReadingTypes(Range<Instant> range);
    List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType);
    List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since);
    List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType , int count);
    List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType , int count);
    boolean hasData();
    boolean is(ReadingContainer other);
    Optional<Meter> getMeter(Instant instant);
    Optional<UsagePoint> getUsagePoint(Instant instant);

    ZoneId getZoneId();

    List<Instant> toList(ReadingType readingType, Range<Instant> exportInterval);
    List<ReadingQualityRecord> getReadingQualities(Set<QualityCodeSystem> qualityCodeSystems, QualityCodeIndex qualityCodeIndex,
                                                   ReadingType readingType, Range<Instant> interval);

    List<? extends MeterActivation> getMeterActivations();
}
