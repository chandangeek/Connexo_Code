package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

import javax.inject.Inject;

import java.util.Date;

public class ReadingQualityRecordImpl implements ReadingQualityRecord {

    private long id;
    private String comment;
    private long channelId;
    private UtcInstant readingTimestamp;
    private String typeCode;
    private boolean actual;

    private transient ReadingQualityType type;
    private transient Optional<BaseReadingRecord> baseReadingRecord;
    private transient Channel channel;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final MeteringService meteringService;
    private final EventService eventService;

    @Inject
    ReadingQualityRecordImpl(DataModel dataModel, MeteringService meteringService, EventService eventService) {
        this.dataModel = dataModel;
        // for persistence
        this.meteringService = meteringService;
        this.eventService = eventService;
        this.actual = true;
    }

    ReadingQualityRecordImpl init(ReadingQualityType type, Channel channel, BaseReadingRecord baseReadingRecord) {
        this.channel = channel;
        this.channelId = channel.getId();
        this.baseReadingRecord = Optional.of(baseReadingRecord);
        readingTimestamp = new UtcInstant(baseReadingRecord.getTimeStamp());
        this.type = type;
        this.typeCode = type.getCode();
        return this;
    }

    ReadingQualityRecordImpl init(ReadingQualityType type, Channel channel, Date timestamp) {
        this.channel = channel;
        this.channelId = channel.getId();
        readingTimestamp = new UtcInstant(timestamp);
        this.type = type;
        this.typeCode = type.getCode();
        return this;
    }

    static ReadingQualityRecordImpl from(DataModel dataModel, ReadingQualityType type, Channel channel, BaseReadingRecord baseReadingRecord) {
        return dataModel.getInstance(ReadingQualityRecordImpl.class).init(type, channel, baseReadingRecord);
    }

    static ReadingQualityRecordImpl from(DataModel dataModel, ReadingQualityType type, Channel channel, Date timestamp) {
        return dataModel.getInstance(ReadingQualityRecordImpl.class).init(type, channel, timestamp);
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
    public String getTypeCode() {
        return typeCode;
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
    public Optional<BaseReadingRecord> getBaseReadingRecord() {
        if (baseReadingRecord == null) {
            baseReadingRecord = getChannel().getReading(getReadingTimestamp());
        }
        return baseReadingRecord;
    }

    public void save() {
        if (id == 0) {
            dataModel.mapper(ReadingQualityRecord.class).persist(this);
            eventService.postEvent(EventType.READING_QUALITY_CREATED.topic(), new LocalEventSource(this));
        } else {
            dataModel.mapper(ReadingQualityRecord.class).update(this);
            eventService.postEvent(EventType.READING_QUALITY_UPDATED.topic(), new LocalEventSource(this));
        }
    }

    @Override
    public Date getReadingTimestamp() {
        return readingTimestamp.toDate();
    }

    @Override
    public void delete() {
        dataModel.mapper(ReadingQualityRecord.class).remove(this);
        eventService.postEvent(EventType.READING_QUALITY_DELETED.topic(), new LocalEventSource(this));
    }

    @Override
    public long getVersion() {
        return version;
    }
    
    public boolean hasEditCategory() {
    	return hasQualityCodeCategory(QualityCodeCategory.EDITED);
    }
    
    public boolean hasValidationCategory() {
    	return hasQualityCodeCategory(QualityCodeCategory.VALIDATION);
    }
    
    public boolean isSuspect() {
    	return hasQualityIndex(QualityCodeIndex.SUSPECT);
    }
    
    public boolean isMissing() {
    	return hasQualityIndex(QualityCodeIndex.KNOWNMISSINGREAD);
    }
    
    private boolean hasQualityCodeCategory(QualityCodeCategory cat) {
    	return getType().category().filter(category -> category.equals(cat)).isPresent();
    }
    
    private boolean hasQualityIndex(QualityCodeIndex index) {
    	return getType().qualityIndex().filter(qualityIndex -> qualityIndex.equals(index)).isPresent();
    }
    
    public void readingValueChanged() {
    	this.actual = false;
    	this.save();
    }
    
    @Override
    public void makeActual() {
    	this.actual = true;
    	this.save();
    }
    
    @Override
    public boolean isActual() {
    	return actual;
    }
    
    public class LocalEventSource {
        private final ReadingQualityRecordImpl readingQuality;

        public LocalEventSource(ReadingQualityRecordImpl readingQuality) {
            this.readingQuality = readingQuality;
        }

        public long getReadingTimestamp() {
            return readingQuality.readingTimestamp.getTime();
        }

        public long getChannelId() {
            return readingQuality.channelId;
        }

        public String getTypeCode() {
            return readingQuality.typeCode;
        }

    }
}
