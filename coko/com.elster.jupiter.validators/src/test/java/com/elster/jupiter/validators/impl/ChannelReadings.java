/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChannelReadings {

    List<IntervalReadingRecord> readings = new ArrayList<>();

    ChannelReadings(int readingsCount) {
        IntStream.rangeClosed(1,readingsCount).forEach(c -> readings.add(null));
    }

    void setReadingValue(int index, BigDecimal value, Instant readingTime) {
        IntervalReadingRecord reading = mock(IntervalReadingRecord.class);
        when(reading.getTimeStamp()).thenReturn(readingTime);
        when(reading.getValue()).thenReturn(value);
        readings.remove(index);
        readings.add(index, reading);
    }

    Channel mockChannel(Range<Instant> range) {
        Channel channel = mock(Channel.class);
        when(channel.getIntervalReadings(range)).thenReturn(readings.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return channel;
    }

}

