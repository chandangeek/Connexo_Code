/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfileReading;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a measurement/reading of all channels of a load profile in a given interval
 * Created by bvn on 8/1/14.
 */
public class LoadProfileReadingImpl implements LoadProfileReading {
    private Range<Instant> interval;
    private Map<Channel, IntervalReadingRecord> values = new HashMap<>();
    private Map<Channel, DataValidationStatus> states = new HashMap<>();
    private Instant readingTime;
    private Map<Channel, List<? extends ReadingQualityRecord>> readingQualities = new HashMap<>();

    @Override
    public Range<Instant> getRange() {
        return interval;
    }

    public void setRange(Range<Instant> interval) {
        this.interval = interval;
    }

    public void setChannelData(Channel channel, IntervalReadingRecord readingRecord) {
        values.put(channel, readingRecord);
    }

    public void setDataValidationStatus(Channel channel, DataValidationStatus status) {
        states.put(channel, status);
    }

    @Override
    public Map<Channel, IntervalReadingRecord> getChannelValues() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public Map<Channel, DataValidationStatus> getChannelValidationStates() {
        return Collections.unmodifiableMap(states);
    }

    @Override
    public Instant getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(Instant reportedDateTime) {
        this.readingTime = reportedDateTime;
    }

    @Override
    public Map<Channel, List<? extends ReadingQualityRecord>> getReadingQualities() {
        return Collections.unmodifiableMap(readingQualities);
    }

    @Override
    public void setReadingQualities(Channel channel, List<? extends ReadingQualityRecord> readingQualities) {
        this.readingQualities.put(channel, readingQualities);
    }

    @Override
    public String toString() {
        return "LoadProfileReadingImpl{" +
                "readingTime=" + readingTime +
                ", interval=" + interval +
                '}';
    }
}