package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.Reading;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

public class RegisterReadingWithValidationStatus extends ReadingWithValidationStatus<ReadingRecord> {

    private ReadingRecord previousReadingRecord;

    public RegisterReadingWithValidationStatus(ZonedDateTime readingTimeStamp) {
        super(readingTimeStamp);
    }

    public Optional<ReadingRecord> getPreviousReading() {
        return Optional.ofNullable(this.previousReadingRecord);
    }

    public void setPreviousReadingRecord(ReadingRecord previousReadingRecord) {
        this.previousReadingRecord = previousReadingRecord;
    }

    public BigDecimal getPreviousValue(){
        return getPreviousReading().map(BaseReading::getValue).orElse(null);
    }

    public BigDecimal getDeltaValue(){
        BigDecimal value = getValue();
        BigDecimal previousValue = getPreviousValue();
        if(value != null && previousValue != null) {
            return value.subtract(previousValue);
        }
        return null;
    }

    public Optional<String> getText() {
        return getPersistedReadingRecord().map(Reading::getText);
    }

    public Optional<Range<Instant>> getBillingPeriod() {
        return getPersistedReadingRecord().flatMap(BaseReading::getTimePeriod);
    }

    public Optional<Range<Instant>> getTimePeriod() {
        if (getReading().get().getReadingType().getMacroPeriod().equals(MacroPeriod.BILLINGPERIOD)) {
            return getReading().get().getTimePeriod();
        } else if (getPreviousReading().isPresent()) {
            return Optional.of(Range.openClosed(getPreviousReading().get().getTimeStamp(), getReading().get().getTimeStamp()));
        } else {
            return Optional.of(Range.atMost(getReading().get().getTimeStamp()));
        }
    }
}
