package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfileReading;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.validation.DataValidationStatus;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
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
    private final List<ProfileStatus.Flag> flags = new ArrayList<>();

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

    public void setReadingTime(Instant reportedDateTime) {
        this.readingTime = reportedDateTime;
    }

    @Override
    public Instant getReadingTime() {
        return readingTime;
    }

    @Override
    public void setFlags(List<ProfileStatus.Flag> flags) {
        this.flags.clear();
        this.flags.addAll(flags);
    }

    @Override
    public List<ProfileStatus.Flag> getFlags() {
        return Collections.unmodifiableList(flags);
    }

    @Override
    public String toString() {
        return "LoadProfileReadingImpl{" +
                "readingTime=" + readingTime +
                ", interval=" + interval +
                '}';
    }
}