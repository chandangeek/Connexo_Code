/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.meterreadings;

import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AggregatedRegisterReading implements ReadingRecord {
    // nullable
    private ReadingRecord calculatedReading;
    // not nullable
    private ReadingRecord reading;

    private AggregatedRegisterReading(ReadingRecord reading) {
        this.reading = reading;
    }

    static AggregatedRegisterReading fromCalculatedReading(ReadingRecord reading) {
        AggregatedRegisterReading aggregated = new AggregatedRegisterReading(reading);
        aggregated.calculatedReading = reading;
        return aggregated;
    }

    static AggregatedRegisterReading fromPersistedReading(ReadingRecord reading) {
        return new AggregatedRegisterReading(reading);
    }

    void setPersistedReading(ReadingRecord reading) {
        this.reading = reading;
    }

    @Override
    public List<Quantity> getQuantities() {
        return reading.getQuantities();
    }

    @Override
    public Quantity getQuantity(int i) {
        return reading.getQuantity(i);
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return reading.getQuantity(readingType);
    }

    @Override
    public ReadingType getReadingType() {
        return reading.getReadingType();
    }

    @Override
    public ReadingType getReadingType(int i) {
        return reading.getReadingType(i);
    }

    @Override
    public List<? extends ReadingType> getReadingTypes() {
        return reading.getReadingTypes();
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return reading.getProcessStatus();
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        reading.setProcessingFlags(flags);
    }

    @Override
    public ReadingRecord filter(ReadingType readingType) {
        return reading.filter(readingType);
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return reading.getSensorAccuracy();
    }

    @Override
    public Instant getTimeStamp() {
        return reading.getTimeStamp();
    }

    @Override
    public Instant getReportedDateTime() {
        return reading.getReportedDateTime();
    }

    @Override
    public BigDecimal getValue() {
        return reading.getValue();
    }

    @Override
    public String getSource() {
        return reading.getSource();
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return reading.getTimePeriod();
    }

    /**
     * Here only aggregation related reading qualities are taken
     * because persisted reading qualities should anyway be found independently & merged:
     * they can be present even if persisted reading is not.
     * @return A {@link List} of data aggregation related {@link ReadingQualityRecord ReadingQualityRecords}.
     */
    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return calculatedReading == null ? Collections.emptyList() : calculatedReading.getReadingQualities();
    }

    @Override
    public String getReason() {
        return reading.getReason();
    }

    @Override
    public String getReadingTypeCode() {
        return reading.getReadingTypeCode();
    }

    @Override
    public String getText() {
        return reading.getText();
    }
}
