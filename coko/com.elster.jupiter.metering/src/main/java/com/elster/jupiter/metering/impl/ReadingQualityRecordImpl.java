/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class ReadingQualityRecordImpl implements ReadingQualityRecord {

    @SuppressWarnings("unused")
    private long id;
    private String comment;
    private Instant readingTimestamp;
    private String typeCode;
    private boolean actual;

    private transient ReadingQualityType type;
    private transient Optional<BaseReadingRecord> baseReadingRecord;
    private Reference<Channel> channel = ValueReference.absent();
    private Reference<IReadingType> readingType = ValueReference.absent();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    ReadingQualityRecordImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.actual = true;
    }

    ReadingQualityRecordImpl init(ReadingQualityType type, CimChannel cimChannel, BaseReading baseReading) {
        this.channel.set(cimChannel.getChannel());
        this.readingType.set((IReadingType) cimChannel.getReadingType());
        if (baseReading instanceof BaseReadingRecord) {
            this.baseReadingRecord = Optional.of((BaseReadingRecord) baseReading);
        }
        readingTimestamp = baseReading.getTimeStamp();
        this.type = type;
        this.typeCode = type.getCode();
        return this;
    }

    ReadingQualityRecordImpl init(ReadingQualityType type, CimChannel cimChannel, BaseReading baseReading, String comment) {
        init(type, cimChannel, baseReading);
        this.comment = comment;
        return this;
    }

    ReadingQualityRecordImpl init(ReadingQualityType type, CimChannel cimChannel, Instant timestamp) {
        this.channel.set(cimChannel.getChannel());
        this.readingType.set((IReadingType) cimChannel.getReadingType());
        readingTimestamp = timestamp;
        this.type = type;
        this.typeCode = type.getCode();
        return this;
    }

    static ReadingQualityRecordImpl from(DataModel dataModel, ReadingQualityType type, CimChannel cimChannel, BaseReading baseReading) {
        return dataModel.getInstance(ReadingQualityRecordImpl.class).init(type, cimChannel, baseReading);
    }

    static ReadingQualityRecordImpl from(DataModel dataModel, ReadingQualityType type, CimChannel cimChannel, Instant timestamp) {
        return dataModel.getInstance(ReadingQualityRecordImpl.class).init(type, cimChannel, timestamp);
    }

    static ReadingQualityRecordImpl from(DataModel dataModel, ReadingQualityType type, CimChannel cimChannel, BaseReading baseReading, String comment) {
        return dataModel.getInstance(ReadingQualityRecordImpl.class).init(type, cimChannel, baseReading, comment);
    }

    @Override
    public CimChannel getCimChannel() {
        return getChannel().getCimChannel(getReadingType()).get();
    }

    @Override
    public ReadingType getReadingType() {
        return readingType.get();
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
    public Instant getTimestamp() {
        return modTime;
    }

    @Override
    public Channel getChannel() {
        return channel.get();
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

    public void update() {
        dataModel.mapper(ReadingQualityRecord.class).update(this);
        notifyUpdated();
    }

    public void update(String... fieldNames) {
        dataModel.mapper(ReadingQualityRecord.class).update(this, fieldNames);
        notifyUpdated();
    }

    void doSave() {
        if (id == 0) {
            dataModel.mapper(ReadingQualityRecord.class).persist(this);
            notifyCreated();
        } else {
            dataModel.mapper(ReadingQualityRecord.class).update(this);
            notifyUpdated();
        }
    }


    static void saveAll(DataModel model, List<ReadingQualityRecordImpl> records) {
        List<ReadingQualityRecord> myRecords = new ArrayList<>(records);
        model.mapper(ReadingQualityRecord.class).persist(myRecords);
        records.forEach(ReadingQualityRecordImpl::notifyCreated);
    }

    static void updateAll(DataModel model, List<ReadingQualityRecord> records) {
        List<ReadingQualityRecord> myRecords = new ArrayList<>(records);
        model.mapper(ReadingQualityRecord.class).persist(myRecords);
        decorate(records.stream()).filterSubType(ReadingQualityRecordImpl.class).forEach(ReadingQualityRecordImpl::notifyUpdated);
    }

    static void deleteAll(DataModel model, List<ReadingQualityRecord> records) {
        List<ReadingQualityRecord> myRecords = new ArrayList<>(records);
        model.mapper(ReadingQualityRecord.class).remove(myRecords);
        decorate(records.stream()).filterSubType(ReadingQualityRecordImpl.class).forEach(ReadingQualityRecordImpl::notifyDeleted);
    }


    @Override
    public Instant getReadingTimestamp() {
        return readingTimestamp;
    }

    @Override
    public void delete() {
        dataModel.mapper(ReadingQualityRecord.class).remove(this);
        notifyDeleted();
    }

    private void notifyCreated() {
        eventService.postEvent(EventType.READING_QUALITY_CREATED.topic(), new LocalEventSource(this));
    }

    private void notifyUpdated() {
        eventService.postEvent(EventType.READING_QUALITY_UPDATED.topic(), new LocalEventSource(this));
    }

    private void notifyDeleted() {
        eventService.postEvent(EventType.READING_QUALITY_DELETED.topic(), new LocalEventSource(this));
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void makePast() {
        doMakePast();
        this.update("actual");
    }

    void doMakePast() {
        this.actual = false;
    }

    @Override
    public void makeActual() {
        this.actual = true;
        this.update("actual");
    }

    @Override
    public boolean isActual() {
        return actual;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && id == ((ReadingQualityRecordImpl) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    void copy(ReadingQualityRecord source) {
        actual = source.isActual();
        comment = source.getComment();
    }

    public class LocalEventSource {
        private final ReadingQualityRecordImpl readingQuality;

        LocalEventSource(ReadingQualityRecordImpl readingQuality) {
            this.readingQuality = readingQuality;
        }

        public long getReadingTimestamp() {
            return readingQuality.readingTimestamp.toEpochMilli();
        }

        public long getChannelId() {
            return readingQuality.channel.get().getId();
        }

        public String getTypeCode() {
            return readingQuality.typeCode;
        }

        public String getReadingType() {
            return readingQuality.readingType.get().getMRID();
        }
    }
}
