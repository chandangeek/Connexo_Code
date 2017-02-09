/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public abstract class BaseReadingRecordImpl implements BaseReadingRecord {
    private final ChannelContract channel;
    private final TimeSeriesEntry entry;

    BaseReadingRecordImpl(ChannelContract channel, TimeSeriesEntry entry) {
        this.channel = channel;
        this.entry = entry;
    }

    TimeSeriesEntry getEntry() {
        return entry;
    }

    Channel getChannel() {
        return channel;
    }

    @Override
    public Instant getTimeStamp() {
        return entry.getTimeStamp();
    }

    @Override
    public Instant getReportedDateTime() {
        return entry.getRecordDateTime();
    }

    abstract int getReadingTypeOffset();

    @Override
    public BigDecimal getValue() {
        return doGetValue(0);
    }

    @Override
    public List<Quantity> getQuantities() {
        // do not use ImmutableList.builder() as getQuantity(i) can return null;
        List<Quantity> result = new ArrayList<>();
        int offset = 0;
        for (ReadingType readingType : channel.getReadingTypes()) {
            result.add(getQuantity(offset++,readingType));
        }
        return result;
    }

    @Override
    public Quantity getQuantity(int offset) {
        return getQuantity(offset,channel.getReadingTypes().get(offset));
    }

    private Quantity getQuantity(int offset, ReadingType readingType) {
    	return ((IReadingType) readingType).toQuantity(doGetValue(offset));
    }

    private BigDecimal doGetValue(int offset) {
        return entry.getBigDecimal(getReadingTypeOffset() + offset);
    }

    int getIndex(ReadingType readingType) {
    	int result = channel.getReadingTypes().indexOf(readingType);
    	if (result < 0) {
    		throw new IllegalArgumentException(MessageFormat.format("ReadingType {0} does not occur on this channel", readingType.getMRID()));
    	}
    	return result;
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        return getQuantity(getIndex(readingType),readingType);
    }

    @Override
    public ReadingType getReadingType() {
        return getReadingType(0);
    }

    @Override
    public ReadingType getReadingType(int offset) {
        return channel.getReadingTypes().get(offset);
    }

    @Override
    public List<IReadingType> getReadingTypes() {
        return channel.getReadingTypes();
    }

    @Override
    public ProcessStatus getProcessStatus() {
        return new ProcessStatus(entry.getLong(0));
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        ProcessStatus status = getProcessStatus();
        ProcessStatus updatedStatus = status.with(flags);

        Object[] values = new Object[entry.size()];
        System.arraycopy(entry.getValues(), 0, values, 0, entry.size());
        values[0] = updatedStatus.getBits();
        channel.getTimeSeries().add(entry.getTimeStamp(), true, values);
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return null;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public Optional<Range<Instant>> getTimePeriod() {
        return channel.getTimePeriod(this, entry.getValues());
    }

    // TODO: check if sorting is always needed here
    public List<? extends ReadingQualityRecord> getReadingQualities() {
        return getChannel().findReadingQualities().atTimestamp(getTimeStamp()).sorted().collect();
    }

}
