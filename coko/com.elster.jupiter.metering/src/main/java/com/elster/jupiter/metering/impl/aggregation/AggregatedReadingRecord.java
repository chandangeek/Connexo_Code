package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Provides an implementation for the {@link BaseReadingRecord} interface for aggregated data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (11:17)
 */
class AggregatedReadingRecord implements BaseReadingRecord {

    private final MeteringService meteringService;
    private Map<IReadingType, Quantity> values;
    private Timestamp localDate;
    private ProcessStatus processStatus;

    @Inject
    AggregatedReadingRecord(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    /**
     * Initializes this {@link AggregatedReadingRecord} from the specified {@link ResultSet}.
     *
     * @param resultSet The ResultSet
     * @return The initialized AggregatedReadingRecord
     */
    AggregatedReadingRecord init(ResultSet resultSet) {
        this.checkNotInitializedYet();
        return this.doInit(resultSet);
    }

    private void checkNotInitializedYet() {
        if (this.values != null) {
            throw new IllegalStateException("Already initialized");
        }
    }

    private AggregatedReadingRecord doInit(ResultSet resultSet) {
        this.values = new TreeMap<>(Comparator.comparing(IReadingType::getMRID));
        return this.doAddFrom(resultSet);
    }

    AggregatedReadingRecord addFrom(ResultSet resultSet) {
        this.checkAlreadyInitialized();
        return this.doAddFrom(resultSet);
    }

    private void checkAlreadyInitialized() {
        if (this.values == null) {
            throw new IllegalStateException("Not initialized yet");
        }
    }

    AggregatedReadingRecord doAddFrom(ResultSet resultSet) {
        try {
            String readingTypeMRID = resultSet.getString(1);
            BigDecimal value = resultSet.getBigDecimal(2);
            this.localDate = resultSet.getTimestamp(3);
            long processStatusBits = resultSet.getLong(4);
            if (this.processStatus == null) {
                this.processStatus = new ProcessStatus(processStatusBits);
            }
            else {
                this.processStatus = this.processStatus.or(new ProcessStatus(processStatusBits));
            }
            IReadingType readingType = this.findReadingTypeOrThrowException(readingTypeMRID);
            this.values.put(readingType, readingType.toQuantity(value));
            return this;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private IReadingType findReadingTypeOrThrowException(String mRID) {
        return this.meteringService
                .getReadingType(mRID)
                .map(IReadingType.class::cast)
                .orElseThrow(() -> new IllegalStateException("Unknown reading type " + mRID + ". Did some component forget to create it?"));
    }

    @Override
    public List<Quantity> getQuantities() {
        return new ArrayList<>(this.values.values());
    }

    @Override
    public Quantity getQuantity(int offset) {
        return this.getQuantities().get(offset);
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return this.values.get(readingType);
    }

    @Override
    public ReadingType getReadingType() {
        return this.values.keySet().iterator().next();
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return this.getReadingTypes().get(offset);
    }

    @Override
    public List<? extends ReadingType> getReadingTypes() {
        return new ArrayList<>(this.values.keySet());
    }

    @Override
    public ProcessStatus getProcesStatus() {
        return this.processStatus;
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        throw new UnsupportedOperationException("Aggregated reading records do not support setting processing flags");
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return Collections.emptyList();
    }

    @Override
    public Instant getTimeStamp() {
        return this.localDate.toInstant();
    }

    @Override
    public Instant getReportedDateTime() {
        return null;
    }

    @Override
    public BigDecimal getValue() {
        return this.values.get(this.getReadingType()).getValue();
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        Instant start = this.getTimeStamp().minus(IntervalLength.from(this.getReadingType()).toTemporalAmount());
        return Optional.of(Range.openClosed(start, this.getTimeStamp()));
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