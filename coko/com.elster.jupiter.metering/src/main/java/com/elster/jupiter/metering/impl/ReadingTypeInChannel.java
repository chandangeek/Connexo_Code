package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;

public class ReadingTypeInChannel {

    private final Reference<Channel> channel = ValueReference.absent();
    @SuppressWarnings("unused")
    private int position;
    private transient ReadingType readingType;
    private String readingTypeMRID;

    private final DataModel dataModel;

    @SuppressWarnings("unused")
    @Inject
    ReadingTypeInChannel(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    ReadingTypeInChannel init(Channel channel, ReadingType readingType, int position) {
        this.channel.set(channel);
        this.position = position;
        this.readingTypeMRID = readingType.getMRID();
        this.readingType = readingType;
        return this;
    }

    static ReadingTypeInChannel from(DataModel dataModel, Channel channel, ReadingType readingType, int position) {
        return dataModel.getInstance(ReadingTypeInChannel.class).init(channel, readingType, position);
    }

    public ReadingType getReadingType() {
        if (readingType == null) {
            readingType = dataModel.mapper(ReadingType.class).getExisting(readingTypeMRID);
        }
        return readingType;
    }
}
