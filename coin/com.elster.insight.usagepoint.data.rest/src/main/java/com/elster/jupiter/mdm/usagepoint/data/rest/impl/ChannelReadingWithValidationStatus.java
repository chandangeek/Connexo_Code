package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;

public class ChannelReadingWithValidationStatus extends ReadingWithValidationStatus<IntervalReadingRecord> {

    private final TemporalAmount intervalLength;

    public ChannelReadingWithValidationStatus(Channel channel, ZonedDateTime readingTimeStamp, ChannelGeneralValidation channelGeneralValidation) {
        super(readingTimeStamp, channelGeneralValidation);
        this.intervalLength = channel.getIntervalLength().get();
    }

    public Range<Instant> getTimePeriod() {
        ZonedDateTime intervalEnd = getReadingTimeStamp();
        ZonedDateTime intervalStart = intervalEnd.minus(this.intervalLength);
        return Range.openClosed(intervalStart.toInstant(), intervalEnd.toInstant());
    }
}
