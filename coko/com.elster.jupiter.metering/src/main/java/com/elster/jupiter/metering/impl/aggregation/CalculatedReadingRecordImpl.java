/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.CalculatedReadingRecord;
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Currying.test;

/**
 * Provides an implementation for the {@link BaseReadingRecord} interface
 * for data that is calculated from data provided by a {@link com.elster.jupiter.metering.UsagePoint}
 * according to the definitions found in a {@link com.elster.jupiter.metering.config.MetrologyContract}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (11:17)
 */
class CalculatedReadingRecordImpl implements CalculatedReadingRecord {

    static final int SUSPECT = 4;
    static final int MISSING = 3;
    static final int ESTIMATED_EDITED = 1;

    private final InstantTruncaterFactory truncaterFactory;
    private final SourceChannelSetFactory sourceChannelSetFactory;

    private String readingTypeMRID;
    private IReadingType readingType;
    private BigDecimal rawValue;    // Maybe null if record was missing
    private Quantity value;         // Maybe null if record was missing
    private Timestamp localDate;    // Maybe null if record was missing
    private Instant timestamp;      // Never null, even if record was missing, in that case the timestamp is taken from the reading quality entity
    private Instant recordTime;     // Maybe null if record was missing
    private Optional<Range<Instant>> timePeriod;
    private UsagePoint usagePoint;
    private long readingQuality;
    private long count;
    private SourceChannelSet sourceChannelSet;
    private Optional<Event> timeOfUseEvent;

    @Inject
    CalculatedReadingRecordImpl(InstantTruncaterFactory truncaterFactory, SourceChannelSetFactory sourceChannelSetFactory) {
        super();
        this.truncaterFactory = truncaterFactory;
        this.sourceChannelSetFactory = sourceChannelSetFactory;
    }

    @Override
    public int compareTo(CalculatedReadingRecord other) {
        return this.getTimeStamp().compareTo(other.getTimeStamp());
    }

    static CalculatedReadingRecordImpl merge(CalculatedReadingRecordImpl r1, CalculatedReadingRecordImpl r2, Instant mergedTimestamp, InstantTruncaterFactory truncaterFactory, SourceChannelSetFactory sourceChannelSetFactory) {
        if (!r1.readingTypeMRID.equals(r2.readingTypeMRID)) {
            throw new IllegalArgumentException("Cannot merge two CalculatedReadingRecords with different reading type");
        }
        if (!Objects.equals(r1.usagePoint, r2.usagePoint)) {
            throw new IllegalArgumentException("Cannot merge two CalculatedReadingRecords with different usage point");
        }
        if (!r2.getTimeStamp().isAfter(r1.getTimeStamp())) {
            if (r1.readingType == null) {
                throw new IllegalStateException("CalculatedReadingRecords can only be merged if ReadingType was injected first: see com.elster.jupiter.metering.impl.aggregation.CalculatedReadingRecord.setReadingType");
            }
            CalculatedReadingRecordImpl merged = new CalculatedReadingRecordImpl(truncaterFactory, sourceChannelSetFactory);
            merged.readingTypeMRID = r1.readingTypeMRID;
            merged.readingType = r1.readingType;
            merged.rawValue = mergeValue(VirtualReadingType.from(r1.readingType).aggregationFunction(), r1.rawValue, r2.rawValue);
            merged.value = merged.readingType.toQuantity(merged.rawValue);
            merged.localDate = r1.localDate;
            merged.timestamp = mergedTimestamp;
            merged.recordTime = max(r1.recordTime, r2.recordTime);
            merged.usagePoint = r1.usagePoint;
            merged.readingQuality = Math.max(r1.readingQuality, r2.readingQuality);
            merged.count = r1.count + r2.count;
            merged.sourceChannelSet = sourceChannelSetFactory.merge(r1.sourceChannelSet, r2.sourceChannelSet);
            merged.timeOfUseEvent = mergeEvent(r1.timeOfUseEvent, r2.timeOfUseEvent);
            return merged;
        } else {
            return merge(r2, r1, mergedTimestamp, truncaterFactory, sourceChannelSetFactory);
        }
    }

    private static Instant max(Instant timestamp1, Instant timestamp2) {
        if (timestamp1.compareTo(timestamp2) < 0) {
            return timestamp2;
        } else {
            return timestamp1;
        }
    }

    private static BigDecimal mergeValue(AggregationFunction function, BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2 != null) {
            return function.applyTo(v1, v2);
        } else if (v1 == null) {
            return v2;
        } else {
            return v1;
        }
    }

    private static Optional<Event> mergeEvent(Optional<Event> event1, Optional<Event> event2) {
        if (event1.isPresent()) {
            return event1;
        } else if (event2.isPresent()) {
            return event2;
        } else {
            return Optional.empty();
        }
    }

    /**
     * Initializes this {@link CalculatedReadingRecordImpl} from the specified {@link ResultSet}.
     *
     * @param resultSet The ResultSet
     * @return The initialized CalculatedReadingRecord
     */
    CalculatedReadingRecordImpl init(ResultSet resultSet, Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        try {
            this.timeOfUseEvent = Optional.empty();
            int columnIndex = 1;
            this.readingTypeMRID = resultSet.getString(columnIndex++);
            this.rawValue = resultSet.getBigDecimal(columnIndex++);
            this.localDate = resultSet.getTimestamp(columnIndex++);
            this.timestamp = Instant.ofEpochMilli(resultSet.getLong(columnIndex++));
            this.recordTime = Instant.ofEpochMilli(resultSet.getLong(columnIndex++));
            this.readingQuality = resultSet.getLong(columnIndex++);
            this.count = resultSet.getLong(columnIndex++);
            this.sourceChannelSet = sourceChannelSetFactory.parseFromString(resultSet.getString(columnIndex++));
            if (this.count != 1 && this.readingQuality == 0) {
                checkCount(deliverablesPerMeterActivation);
            }
            return this;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void checkCount(Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation) {
        Optional<MeterActivationSet> meterActivationSet =
                deliverablesPerMeterActivation.keySet().stream().filter(maSet -> maSet.contains(this.timestamp)).findAny();
        if (meterActivationSet.isPresent()) {
            List<ReadingTypeDeliverableForMeterActivationSet> deliverables = deliverablesPerMeterActivation.get(meterActivationSet.get());
            Optional<ReadingTypeDeliverableForMeterActivationSet> readingTypeDeliverableForMeterActivationSet =
                    deliverables
                            .stream()
                            .filter(test(this::readingTypeMatches).with(readingTypeMRID))
                            .findFirst();
            if (readingTypeDeliverableForMeterActivationSet.isPresent()) {
                long expectedCount = readingTypeDeliverableForMeterActivationSet.get().getExpectedCount(this.timestamp);
                if (this.count != expectedCount) {
                    List<? extends ReadingQualityRecord> qualities =
                            readingTypeDeliverableForMeterActivationSet.get().getReadingQualities(this.timestamp);
                    this.readingQuality =
                            qualities
                                    .stream()
                                    .filter(ReadingQualityRecord::isSuspect)
                                    .findAny()
                                    .map(r -> SUSPECT)
                                    .orElse(MISSING);
                }
            }
        }
    }

    /**
     * Initializes this {@link CalculatedReadingRecordImpl}
     * and mark it as being part of a time of use gap.
     *
     * @param usagePoint The UsagePoint
     * @param readingType The ReadingType
     * @param timestamp The utc timestamp
     * @param event The Event
     * @return The initialized CalculatedReadingRecord
     */
    CalculatedReadingRecordImpl initAsPartOfGapAt(UsagePoint usagePoint, IReadingType readingType, Instant timestamp, Event event) {
        this.usagePoint = usagePoint;
        this.timeOfUseEvent = Optional.of(event);
        this.readingType = readingType;
        this.readingTypeMRID = readingType.getMRID();
        this.rawValue = null;
        this.localDate = new java.sql.Timestamp(timestamp.toEpochMilli());
        this.timestamp = timestamp;
        this.recordTime = null; // Records that are part of a gap don't actually exist so should not have a record time
        this.readingQuality = 0;
        this.count = 0; // Not expecting any interval when record is part of a time of use gap
        this.sourceChannelSet = sourceChannelSetFactory.empty();
        return this;
    }

    private boolean readingTypeMatches(ReadingTypeDeliverableForMeterActivationSet set, String readingTypeMRID) {
        return set.getDeliverable().getReadingType().getMRID().equals(readingTypeMRID);
    }

    /**
     * Returns a copy of this CalculatedReadingRecord for the specified timestamp.
     *
     * @param timeStamp The Timestamp
     * @return The copied CalculatedReadingRecord that occurs on the specified timestamp
     */
    CalculatedReadingRecordImpl atTimeStamp(Instant timeStamp) {
        CalculatedReadingRecordImpl record = new CalculatedReadingRecordImpl(this.truncaterFactory, this.sourceChannelSetFactory);
        record.usagePoint = this.usagePoint;
        record.rawValue = this.rawValue;
        record.readingTypeMRID = this.readingTypeMRID;
        record.setReadingType(this.readingType);
        record.localDate = new java.sql.Timestamp(timeStamp.toEpochMilli());
        record.timestamp = timeStamp;
        record.recordTime = this.recordTime;
        record.readingQuality = this.readingQuality;
        record.count = 1;
        record.sourceChannelSet = this.sourceChannelSet;
        record.timeOfUseEvent = this.timeOfUseEvent;
        return record;
    }

    void setReadingType(IReadingType readingType) {
        if (!readingType.getMRID().equals(this.readingTypeMRID)) {
            throw new IllegalArgumentException("ReadingType mRID does not match, was expecting " + this.readingTypeMRID + " but got " + readingType.getMRID());
        }
        this.readingType = readingType;
        this.value = readingType.toQuantity(this.rawValue);
    }

    @Override
    public List<Quantity> getQuantities() {
        return Collections.singletonList(this.value);
    }

    @Override
    public Quantity getQuantity(int offset) {
        return this.value;
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        if (this.readingType.equals(readingType)) {
            return this.value;
        } else {
            return null;
        }
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType;
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return this.getReadingTypes().get(offset);
    }

    @Override
    public List<? extends ReadingType> getReadingTypes() {
        return Collections.singletonList(this.readingType);
    }

    @Override
    public ProcessStatus getProcessStatus() {
        ProcessStatus processStatus = new ProcessStatus(0);
        if (readingQuality == SUSPECT) {
            return processStatus.with(ProcessStatus.Flag.SUSPECT);
        } else if (readingQuality == ESTIMATED_EDITED) {
            return processStatus.with(ProcessStatus.Flag.EDITED, ProcessStatus.Flag.ESTIMATED);
        }
        return processStatus;
    }

    public long getReadingQuality() {
        return this.readingQuality;
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        throw new UnsupportedOperationException("Aggregated reading records do not support setting processing flags");
    }

    public long getCount() {
        return count;
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        Optional<ReadingQualityRecord> aggregatedReadingQuality = getAggregatedReadingQuality();
        if (aggregatedReadingQuality.isPresent()) {
            List<ReadingQualityRecord> readingQualitiesFromSourceChannels = fetchReadingQualitiesFromSourceChannels();
            List<ReadingQualityRecord> readingQualities = new ArrayList<>(readingQualitiesFromSourceChannels);
            readingQualities.add(aggregatedReadingQuality.get());
            return readingQualities;
        } else {
            return Collections.emptyList();
        }
    }

    private Optional<ReadingQualityRecord> getAggregatedReadingQuality() {
        ReadingQuality readingQualityValue = null;
        if (readingQuality == SUSPECT) {
            readingQualityValue = ReadingQuality.DERIVED_SUSPECT;
        } else if (readingQuality == MISSING) {
            readingQualityValue = ReadingQuality.DERIVED_MISSING;
        } else if (readingQuality == ESTIMATED_EDITED) {
            readingQualityValue = ReadingQuality.DERIVED_INDETERMINISTIC;
        }
        return Optional.ofNullable(readingQualityValue)
                .map(ReadingQuality::getCode)
                .map(ReadingQualityType::new)
                .map(this::newAggregatedReadingQuality);
    }

    private AggregatedReadingQualityImpl newAggregatedReadingQuality(ReadingQualityType readingQualityType) {
        return new AggregatedReadingQualityImpl(this.readingType, readingQualityType, this.timestamp);
    }

    private List<ReadingQualityRecord> fetchReadingQualitiesFromSourceChannels() {
        Range<Instant> timePeriod = getTimePeriod().orElse(Range.singleton(getTimeStamp()));
        return this.sourceChannelSet.fetchReadingQualities(timePeriod)
                .map(ReadingQualityRecord::getType)
                .distinct()
                .map(this::newAggregatedReadingQuality)
                .collect(Collectors.toList());
    }

    Timestamp getLocalDate() {
        return new Timestamp(localDate.getTime());
    }

    @Override
    public Instant getTimeStamp() {
        return this.timestamp;
    }

    @Override
    public Instant getReportedDateTime() {
        return this.recordTime;
    }

    @Override
    public BigDecimal getValue() {
        return this.rawValue;
    }

    public void setUsagePoint(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        if (this.timePeriod == null) {
            Optional<MeterActivation> meterActivation = this.usagePoint.getMeterActivations(this.getTimeStamp())
                    .stream()
                    .findFirst();
            if (meterActivation.isPresent()) {
                this.timePeriod = this.getTimePeriod(meterActivation.get().getStart(), meterActivation.get().getChannelsContainer().getZoneId());
            } else {
                ZoneId zoneId = ZoneId.of("UTC");
                IntervalLength intervalLength = IntervalLength.from(this.getReadingType());
                Instant start = truncaterFactory.truncaterFor(this.getReadingType()).truncate(this.getTimeStamp(), intervalLength, zoneId);
                this.timePeriod = this.getTimePeriod(start, zoneId);
            }
        }
        return this.timePeriod;
    }

    private Optional<Range<Instant>> getTimePeriod(Instant start, ZoneId zoneId) {
        IntervalLength intervalLength = IntervalLength.from(this.getReadingType());
        if (IntervalLength.NOT_SUPPORTED == intervalLength) {
            return Optional.of(Range.singleton(getTimeStamp()));
        }
        InstantTruncater truncater = truncaterFactory.truncaterFor(this.getReadingType());
        Instant truncatedTimestamp = truncater.truncate(this.getTimeStamp(), intervalLength, zoneId);
        Instant startCandidate;
        Instant endCandidate;
        if (truncatedTimestamp.equals(this.getTimeStamp())) {
            // Timestamp was aligned with interval
            startCandidate = intervalLength.subtractFrom(truncatedTimestamp, zoneId);
            endCandidate = truncatedTimestamp;
        } else {
            startCandidate = truncatedTimestamp;
            endCandidate = intervalLength.addTo(truncatedTimestamp, zoneId);
        }
        Optional<MeterActivation> meterActivationAtStart = this.usagePoint.getMeterActivations(startCandidate).stream().findFirst();
        if (meterActivationAtStart.isPresent()) {
            if (zoneId.equals(meterActivationAtStart.get().getChannelsContainer().getZoneId())) {
                // Same ZoneId
                return Optional.of(Range.openClosed(startCandidate, endCandidate));
            } else {
                // Different ZoneId is not supported yet
                throw new IllegalStateException("UsagePoint has meters that are activated in different TimeZones");
            }
        } else {
            // No meter activation, clip TimePeriod to start of meter activation
            return Optional.of(Range.openClosed(start, endCandidate));
        }
    }

    @Override
    public BaseReadingRecord filter(ReadingType readingType) {
        return null;
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return null;
    }

    @Override
    public String getSource() {
        return null;
    }

    SourceChannelSet getSourceChannelSet() {
        return sourceChannelSet;
    }

    @Override
    public boolean isPartOfTimeOfUseGap() {
        return this.timeOfUseEvent.isPresent();
    }

    @Override
    public Optional<Event> getTimeOfUseEvent() {
        return this.timeOfUseEvent;
    }

}