/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.entity;


import com.elster.jupiter.servicecall.issue.NotEstimatedBlock;

import java.time.Instant;

public class NotEstimatedBlockImpl implements NotEstimatedBlock {
    
    public enum Fields {
        ISSUE("issue"),
        CHANNEL("channel"),
        READINGTYPE("readingType"),
        STARTTIME("startTime"),
        ENDTIME("endTime");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

//    @IsPresent
//    private Reference<Channel> channel = ValueReference.absent();
//
//    @IsPresent
//    private Reference<ReadingType> readingType = ValueReference.absent();

    private Instant startTime;

    private Instant endTime;

//    NotEstimatedBlockImpl init(Channel channel, ReadingType readingType, Instant startTime, Instant endTime) {
//        this.channel.set(channel);
////        this.readingType.set(readingType);
//        this.startTime = startTime;
//        this.endTime = endTime;
//        return this;
//    }

//    @Override
//    public Channel getChannel() {
//        return channel.get();
//    }
//
//    @Override
//    public ReadingType getReadingType() {
//        return readingType.get();
//    }
    
    @Override
    public Instant getStartTime() {
        return startTime;
    }
    
    @Override
    public Instant getEndTime() {
        return endTime;
    }
}
