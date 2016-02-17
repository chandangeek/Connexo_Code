package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link BaseReadingRecord} interface for aggregated data.
 * Todo: Complete implementation of the BaseReadingRecord methods that have been generated for now
 *       to be able to run tests for other components
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (11:17)
 */
class AggregatedReadingRecord implements BaseReadingRecord {

    /**
     * Constructs an {@link AggregatedReadingRecord} from the specified {@link ResultSet}.
     *
     * @param resultSet The ResultSet
     * @return The AggregatedReadingRecord
     */
    static AggregatedReadingRecord from(ResultSet resultSet) {
        return null;
    }

    @Override
    public List<Quantity> getQuantities() {
        return Collections.emptyList();
    }

    @Override
    public Quantity getQuantity(int offset) {
        return null;
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return null;
    }

    @Override
    public ReadingType getReadingType() {
        return null;
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return null;
    }

    @Override
    public List<? extends ReadingType> getReadingTypes() {
        return Collections.emptyList();
    }

    @Override
    public ProcessStatus getProcesStatus() {
        return null;
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {

    }

    @Override
    public BaseReadingRecord filter(ReadingType readingType) {
        return null;
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return Collections.emptyList();
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return null;
    }

    @Override
    public Instant getTimeStamp() {
        return null;
    }

    @Override
    public Instant getReportedDateTime() {
        return null;
    }

    @Override
    public BigDecimal getValue() {
        return null;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return Optional.empty();
    }

}