package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ProcesStatus;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
        for (int i = 0; i < entry.size() - getReadingTypeOffset(); i++) {
            result.add(getQuantity(i));
        }
        return result;
    }

    @Override
    public Quantity getQuantity(int offset) {
        ReadingTypeImpl readingType = channel.getReadingTypes().get(offset);
        return readingType.toQuantity(doGetValue(offset));
    }

    private BigDecimal doGetValue(int offset) {
        return entry.getBigDecimal(getReadingTypeOffset() + offset);
    }

    @Override
    public Quantity getQuantity(ReadingType readingType) {
        int i = 0;
        for (ReadingTypeImpl each : channel.getReadingTypes()) {
            if (each.equals(readingType)) {
                return each.toQuantity(doGetValue(i));
            }
            i++;
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
    public ProcesStatus getProcesStatus() {
        return new ProcesStatus(entry.getLong(0));
    }

    @Override
    public void setProcessingFlags(ProcesStatus.Flag... flags) {
        ProcesStatus status = getProcesStatus();
        ProcesStatus updatedStatus = status.with(flags);

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

}
