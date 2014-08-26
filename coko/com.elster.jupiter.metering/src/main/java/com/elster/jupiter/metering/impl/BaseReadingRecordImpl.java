package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ProcessStatus;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;


public abstract class BaseReadingRecordImpl implements BaseReadingRecord {
    private final ChannelImpl channel;
    private final TimeSeriesEntry entry;

    BaseReadingRecordImpl(ChannelImpl channel, TimeSeriesEntry entry) {
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
    public Date getTimeStamp() {
        return entry.getTimeStamp();
    }

    @Override
    public Date getReportedDateTime() {
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
    	return ((ReadingTypeImpl) readingType).toQuantity(doGetValue(offset));
    }

    private BigDecimal doGetValue(int offset) {
        return entry.getBigDecimal(getReadingTypeOffset() + offset);
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        int i = channel.getReadingTypes().indexOf(readingType);
        if (i >= 0) {
        	return getQuantity(i,readingType);
        } 
        throw new IllegalArgumentException(MessageFormat.format("ReadingType {0} does not occur on this channel", readingType.getMRID()));
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
    public List<ReadingTypeImpl> getReadingTypes() {
        return channel.getReadingTypes();
    }

    @Override
    public ProcessStatus getProcesStatus() {
        return new ProcessStatus(entry.getLong(0));
    }

    @Override
    public void setProcessingFlags(ProcessStatus.Flag... flags) {
        ProcessStatus status = getProcesStatus();
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
    public Interval getTimePeriod() {
        return null;
    }
    
    public List<? extends ReadingQuality> getReadingQualities() {
    	return getChannel().findReadingQuality(getTimeStamp());
    }

}
