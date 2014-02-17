package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;
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

    private final DataModel dataModel;
    private final MeteringService meteringService;

    @Inject
    ReadingQualityImpl(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        // for persistence
        this.meteringService = meteringService;
    }

    ReadingQualityImpl init(ReadingQualityType type, Channel channel, BaseReadingRecord baseReadingRecord) {
        this.channel = channel;
        this.channelId = channel.getId();
        this.baseReadingRecord = baseReadingRecord;
        readingTimestamp = new UtcInstant(baseReadingRecord.getTimeStamp());
        this.type = type;
        this.typeCode = type.getCode();
        return this;
    }

    static ReadingQualityImpl from(DataModel dataModel, ReadingQualityType type, Channel channel, BaseReadingRecord baseReadingRecord) {
        return dataModel.getInstance(ReadingQualityImpl.class).init(type, channel, baseReadingRecord);
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
            channel = meteringService.findChannel(channelId).get();
        }
        return channel;
    }

    @Override
    public ReadingQualityType getType() {
        if (type == null) {
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
            dataModel.mapper(ReadingQuality.class).persist(this);
        } else {
            dataModel.mapper(ReadingQuality.class).update(this);
        }
    }

    @Override
    public Date getReadingTimestamp() {
        return readingTimestamp.toDate();
    }
}
