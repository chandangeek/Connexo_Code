package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.Date;

public class ReadingQualityImpl implements ReadingQuality {

    private long id;
    private String comment;
    private long channelId;
    private UtcInstant readingTimestamp;
    private String typeCode;

    private transient ReadingQualityType type;
    private transient BaseReadingRecord baseReadingRecord;
    private transient Channel channel;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private ReadingQualityImpl() {
        // for persistence
    }

    public ReadingQualityImpl(ReadingQualityType type, Channel channel, BaseReadingRecord baseReadingRecord) {
        this.channel = channel;
        this.channelId = channel.getId();
        this.baseReadingRecord = baseReadingRecord;
        readingTimestamp = new UtcInstant(baseReadingRecord.getTimeStamp());
        this.type = type;
        this.typeCode = type.getCode();
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public Date getTimestamp() {
        return modTime.toDate();
    }

    @Override
    public Channel getChannel() {
        if (channel == null) {
            channel = Bus.getMeteringService().findChannel(channelId).get();
        }
        return channel;
    }

    @Override
    public ReadingQualityType getType() {
        if (typeCode == null) {
            type = new ReadingQualityType(typeCode);
        }
        return type;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public BaseReadingRecord getBaseReadingRecord() {
        if (baseReadingRecord == null) {
            baseReadingRecord = getChannel().getReading(getTimestamp()).get();
        }
        return baseReadingRecord;
    }

    public void save() {
        if (id == 0) {
            Bus.getOrmClient().getReadingQualityFactory().persist(this);
        } else {
            Bus.getOrmClient().getReadingQualityFactory().update(this);
        }
    }
}
