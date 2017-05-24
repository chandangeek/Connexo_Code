package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.DataValidationStatus;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// public class ChannelReadingWithValidationStatus extends ReadingWithValidationStatus<IntervalReadingRecord> {
public class ChannelReadingWithValidationStatus {

    private final ZonedDateTime readingTimeStamp;
    private AggregatedChannel.AggregatedIntervalReadingRecord readingRecord;
    private final ChannelGeneralValidation channelGeneralValidation;
    private DataValidationStatus validationStatus;
    private final TemporalAmount intervalLength;
    private final Optional<Calendar> calendar;
    private boolean partOfTimeOfUseGap = false;

    public ChannelReadingWithValidationStatus(Channel channel, ZonedDateTime readingTimeStamp, ChannelGeneralValidation channelGeneralValidation, Optional<Calendar> calendar) {
        super();
        this.readingTimeStamp = readingTimeStamp;
        this.channelGeneralValidation = channelGeneralValidation;
        this.intervalLength = channel.getIntervalLength().get();
        this.calendar = calendar;
    }

    public Range<Instant> getTimePeriod() {
        ZonedDateTime intervalStart = this.readingTimeStamp.minus(this.intervalLength);
        return Range.openClosed(intervalStart.toInstant(), this.readingTimeStamp.toInstant());
    }

    public Optional<Calendar> getCalendar() {
        return this.calendar;
    }

    public Optional<DataValidationStatus> getValidationStatus() {
        return Optional.ofNullable(this.validationStatus);
    }

    public void setValidationStatus(DataValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public Optional<Pair<ReadingModificationFlag, ReadingQuality>> getReadingModificationFlag() {
        if (this.validationStatus != null) {
            return Optional.ofNullable(
                    ReadingModificationFlag.getModificationFlagWithQualityRecord(
                            this.validationStatus.getReadingQualities(),
                            Optional.ofNullable(this.readingRecord)
                    )
            );
        } else {
            return Optional.empty();
        }
    }

    public List<? extends ReadingQuality> getReadingQualities() {
        if (this.readingRecord != null) {
            List<ReadingQuality> readingQualities = new ArrayList<>(this.readingRecord.getReadingQualities().stream()
                    .filter(ReadingQualityRecord::isActual)
                    .map(q -> (ReadingQuality) q)
                    .collect(Collectors.toList()));
            if (this.validationStatus != null) {
                List<ReadingQuality> persistedReadingQualities = this.validationStatus.getReadingQualities().stream()
                        .map(ReadingQuality.class::cast)
                        .filter(readingQuality -> !ReadingWithValidationStatus.hasValidatedOkReadingQualityType(readingQuality))
                        .filter(readingQuality -> !readingQualities.contains(readingQuality))
                        .collect(Collectors.toList());
                readingQualities.addAll(persistedReadingQualities);
            }
            return readingQualities;
        } else {
            return Collections.emptyList();
        }
    }

    public boolean isChannelValidationActive() {
        return this.channelGeneralValidation.isValidationActive();
    }

    public Optional<Instant> getChannelLastChecked() {
        return Optional.ofNullable(this.channelGeneralValidation.getLastChecked());
    }

    public void setReadingRecord(AggregatedChannel.AggregatedIntervalReadingRecord readingRecord) {
        this.readingRecord = readingRecord;
        if (readingRecord.isPartOfTimeOfUseGap()) {
            this.markPartOfTimeOfUseGap();
        }
    }

    public BigDecimal getValue() {
        if (this.readingRecord == null) {
            return null;
        } else {
            return this.readingRecord.getValue();
        }
    }

    public boolean wasEdited() {
        if (this.readingRecord == null) {
            return false;
        } else {
            return this.readingRecord.wasEdited();
        }
    }

    public BigDecimal getCalculatedValue() {
        if (this.readingRecord == null) {
            return null;
        } else {
            return this.readingRecord.getOriginalValue();
        }
    }

    public Instant getTimeStamp() {
        return this.readingTimeStamp.toInstant();
    }

    public Instant getReportedDateTime() {
        if (this.readingRecord == null) {
            return null;
        } else {
            return this.readingRecord.getReportedDateTime();
        }
    }

    public boolean isPartOfTimeOfUseGap() {
        return this.partOfTimeOfUseGap;
    }

    public void markPartOfTimeOfUseGap() {
        this.partOfTimeOfUseGap = true;
    }

}