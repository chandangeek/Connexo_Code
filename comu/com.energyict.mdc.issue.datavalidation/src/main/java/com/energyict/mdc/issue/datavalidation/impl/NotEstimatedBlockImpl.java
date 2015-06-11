package com.energyict.mdc.issue.datavalidation.impl;

import java.time.Instant;

import javax.validation.constraints.NotNull;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;

public class NotEstimatedBlockImpl implements NotEstimatedBlock {
    
    public enum Fields {
        ISSUE("issue"),
        CHANNEL("channel"),
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

    @IsPresent
    private Reference<Channel> channel = ValueReference.absent();
    
    @NotNull
    private ReadingType readingType;

    NotEstimatedBlockImpl init(Channel channel, ReadingType readingType, Instant startTime, Instant endTime) {
        this.channel.set(channel);
        this.readingType = readingType;
        this.startTime = startTime;
        this.endTime = endTime;
        return this;
    }
    
    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    @Override
    public Channel getChannel() {
        return channel.get();
    }
    
    @Override
    public ReadingType getReadingType() {
        return readingType;
    }
    
    @Override
    public Instant getStartTime() {
        return startTime;
    }
    
    @Override
    public Instant getEndTime() {
        return endTime;
    }
}
