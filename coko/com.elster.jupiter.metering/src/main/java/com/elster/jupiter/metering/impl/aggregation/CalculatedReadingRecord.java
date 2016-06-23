package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides an implementation for the {@link BaseReadingRecord} interface
 * for data that is calculated from data provided by a {@link com.elster.jupiter.metering.UsagePoint}
 * according to the definitions found in a {@link com.elster.jupiter.metering.config.MetrologyContract}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (11:17)
 */
class CalculatedReadingRecord implements BaseReadingRecord {

    private String readingTypeMRID;
    private IReadingType readingType;
    private BigDecimal rawValue;
    private Quantity value;
    private Timestamp localDate;
    private Instant timestamp;
    private UsagePoint usagePoint;
    private ProcessStatus processStatus;
    private long count;

    static CalculatedReadingRecord merge(CalculatedReadingRecord r1, CalculatedReadingRecord r2, Instant mergedTimestamp) {
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
            CalculatedReadingRecord merged = new CalculatedReadingRecord();
            merged.readingTypeMRID = r1.readingTypeMRID;
            merged.readingType = r1.readingType;
            merged.rawValue = mergeValue(VirtualReadingType.from(r1.readingType).aggregationFunction(), r1.rawValue, r2.rawValue);
            merged.value = merged.readingType.toQuantity(merged.rawValue);
            merged.localDate = r1.localDate;
            merged.timestamp = mergedTimestamp;
            merged.usagePoint = r1.usagePoint;
            merged.processStatus = r1.processStatus.or(r2.processStatus);
            merged.count = r1.count + r2.count;
            return merged;
        } else {
            return merge(r2, r1, mergedTimestamp);
        }
    }

    private static BigDecimal mergeValue(AggregationFunction function, BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2!= null) {
            return function.applyTo(v1, v2);
        } else if (v1 == null) {
            return v2;
        } else {
            return v1;
        }
    }

    /**
     * Initializes this {@link CalculatedReadingRecord} from the specified {@link ResultSet}.
     *
     * @param resultSet The ResultSet
     * @return The initialized AggregatedReadingRecord
     */
    CalculatedReadingRecord init(ResultSet resultSet) {
        try {
            int columnIndex = 1;
            this.readingTypeMRID = resultSet.getString(columnIndex++);
            this.rawValue = resultSet.getBigDecimal(columnIndex++);
            this.localDate = resultSet.getTimestamp(columnIndex++);
            this.timestamp = Instant.ofEpochMilli(resultSet.getLong(columnIndex++));
            this.processStatus = new ProcessStatus(resultSet.getLong(columnIndex++));
            this.count = resultSet.getLong(columnIndex);
            return this;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
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
    public ProcessStatus getProcesStatus() {
        return this.processStatus;
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
        return Collections.emptyList();
    }

    public Timestamp getLocalDate() {
        return new Timestamp(localDate.getTime());
    }

    @Override
    public Instant getTimeStamp() {
        return this.timestamp;
    }

    @Override
    public Instant getReportedDateTime() {
        return null;
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
        MeterActivation meterActivation = this.usagePoint.getMeterActivations(this.getTimeStamp())
                .stream()
                .findFirst()
                .get();
        ZoneId zoneId = meterActivation.getZoneId();
        IntervalLength intervalLength = IntervalLength.from(this.getReadingType());
        Instant startCandidate = intervalLength.truncate(this.getTimeStamp(), zoneId);
        if (startCandidate.equals(this.getTimeStamp())) {
            // Timestamp was aligned with interval
            startCandidate = intervalLength.subtractFrom(startCandidate, zoneId);
        }
        Optional<MeterActivation> meterActivationAtStart = this.usagePoint.getMeterActivations(startCandidate)
                .stream()
                .findFirst();
        if (meterActivationAtStart.isPresent()) {
            if (zoneId.equals(meterActivationAtStart.get().getZoneId())) {
                // Same ZoneId
                return Optional.of(Range.openClosed(startCandidate, this.getTimeStamp()));
            } else {
                // Different ZoneId is not supported yet
                throw new IllegalStateException("UsagePoint has meters that are activated in different TimeZones");
            }
        } else {
            // No meter activation, clip TimePeriod to start of meter activation
            return Optional.of(Range.openClosed(meterActivation.getStart(), this.getTimeStamp()));
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

}