package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

public class ChannelReadingWithValidationStatus extends ReadingWithValidationStatus<IntervalReadingRecord> {

    private final TemporalAmount intervalLength;
    private final Optional<Calendar> calendar;
    private boolean partOfTimeOfUseGap = false;

    public ChannelReadingWithValidationStatus(Channel channel, ZonedDateTime readingTimeStamp, ChannelGeneralValidation channelGeneralValidation, Optional<Calendar> calendar) {
        super(readingTimeStamp, channelGeneralValidation);
        this.intervalLength = channel.getIntervalLength().get();
        this.calendar = calendar;
    }

    public Range<Instant> getTimePeriod() {
        ZonedDateTime intervalEnd = getReadingTimeStamp();
        ZonedDateTime intervalStart = intervalEnd.minus(this.intervalLength);
        return Range.openClosed(intervalStart.toInstant(), intervalEnd.toInstant());
    }

    public Optional<Calendar> getCalendar(){
        return this.calendar;
    }

    @Override
    public void setCalculatedReadingRecord(IntervalReadingRecord readingRecord) {
        super.setCalculatedReadingRecord(readingRecord);
        if (readingRecord instanceof AggregatedChannel.AggregatedIntervalReadingRecord) {
            AggregatedChannel.AggregatedIntervalReadingRecord aggregated = (AggregatedChannel.AggregatedIntervalReadingRecord) readingRecord;
            if (aggregated.isPartOfTimeOfUseGap()) {
                this.markPartOfTimeOfUseGap();
            }
        }
    }

    public boolean isPartOfTimeOfUseGap() {
        return this.partOfTimeOfUseGap;
    }

    public void markPartOfTimeOfUseGap() {
        this.partOfTimeOfUseGap = true;
    }

}